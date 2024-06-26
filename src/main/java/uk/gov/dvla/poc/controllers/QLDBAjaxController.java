package uk.gov.dvla.poc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.dvla.poc.events.ContactDetailsChanged;
import uk.gov.dvla.poc.events.NameChanged;
import uk.gov.dvla.poc.events.PenaltyPointsAdded;
import uk.gov.dvla.poc.events.PenaltyPointsRemoved;
import uk.gov.dvla.poc.forms.BicycleLicenceQueryForm;
import uk.gov.dvla.poc.forms.BicycleLicenceUpdateForm;
import uk.gov.dvla.poc.forms.LicenceIdQueryForm;
import uk.gov.dvla.poc.forms.VerifyLicenceForm;
import uk.gov.dvla.poc.model.BicycleLicence;
import uk.gov.dvla.poc.model.HistoryResult;
import uk.gov.dvla.poc.model.dynamo.Activity;
import uk.gov.dvla.poc.model.dynamo.LicenceActivity;
import uk.gov.dvla.poc.repository.BicycleLicenceQLDBRepository;
import uk.gov.dvla.poc.repository.LicenceActivityDynamoRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@Slf4j
public class QLDBAjaxController {

    private BicycleLicenceQLDBRepository licenceQLDBRepository;

    private LicenceActivityDynamoRepository licenceActivityDynamoRepository;

    public QLDBAjaxController(BicycleLicenceQLDBRepository licenceQLDBRepository, LicenceActivityDynamoRepository licenceActivityDynamoRepository) {
        this.licenceQLDBRepository = licenceQLDBRepository;
        this.licenceActivityDynamoRepository = licenceActivityDynamoRepository;
    }

    /**
     * Ajax Post
     * @param form
     * @param model
     * @return
     */
    @PostMapping("/queryLicence")
    public String queryLicence(@ModelAttribute BicycleLicenceQueryForm form, Model model) throws JsonProcessingException {
        String email = form.getEmail();
        BicycleLicence licence = licenceQLDBRepository.findByEmail(email);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(licence);
    }

    /**
     * Ajax Post
     * @param form
     * @param model
     * @return
     */
    @PostMapping("/updateLicenceForm")
    public String updateLicenceForm(HttpServletRequest request, @ModelAttribute BicycleLicenceUpdateForm form, Model model) {
        List<BicycleLicence> licences = (List<BicycleLicence>) request.getSession().getAttribute("licences");
        BicycleLicence existingLicence = StreamSupport.stream(licences.spliterator(), false)
                .filter(licence -> form.getEmail().equals(licence.getEmail()))
                .findAny()
                .orElse(null);

        if(existingLicence != null) {
            BicycleLicence licence = new BicycleLicence();
            licence.setEmail(form.getEmail());
            licence.setName(form.getName());
            licence.setTelephone(form.getTelephone());
            licence.setPenaltyPoints(form.getPenaltyPoints());

            if(form.getPenaltyPoints() > existingLicence.getPenaltyPoints()) {
                licence.addEvent(new PenaltyPointsAdded());
            } else if (form.getPenaltyPoints() < existingLicence.getPenaltyPoints()) {
                licence.addEvent(new PenaltyPointsRemoved());
            }

            if(!form.getName().equalsIgnoreCase(existingLicence.getName())) {
                licence.addEvent(new NameChanged());
            }
            if(!form.getTelephone().equalsIgnoreCase(existingLicence.getTelephone())) {
                licence.addEvent(new ContactDetailsChanged());
            }

            BicycleLicence committedLicence = licenceQLDBRepository.update(licence);
            return committedLicence.toString();
        } else {
            return "fail";
        }

    }

    /**
     * Ajax Post
     * @return
     */
    @GetMapping("/history")
    public ModelAndView queryHistory(HttpServletRequest request, @RequestParam String email, @RequestParam String docId, Model model) throws JsonProcessingException {
        log.info("Email " + email);
        log.info("Doc Id " + docId);
        List<HistoryResult> revisions = null;
        if(docId != null && !docId.equals("")) {
            revisions = licenceQLDBRepository.findAllRevisionsByDocumentId(docId);
        }
        else {
            revisions = licenceQLDBRepository.findAllRevisionsByEmail(email);
        }
        if(revisions.size() > 1) {
            List<HistoryResult> sortedRevisions = revisions.stream().sorted((v1, v2) -> Integer.compare(v2.getVersion(), v1.getVersion())).collect(Collectors.toList());
            model.addAttribute("revisions", sortedRevisions);
            request.getSession().setAttribute("revisions", sortedRevisions);
        } else
        {
            model.addAttribute("revisions", revisions);
            request.getSession().setAttribute("revisions", revisions);
        }

        return new ModelAndView("licenceMgmt :: historyResultsList");
    }

    /**
     * Ajax Post
     * @param model
     * @return
     */
    @PostMapping("/verifyLicenceForm")
    public boolean verify(HttpServletRequest request, @RequestParam int index, Model model) {
        List<HistoryResult> results = (List<HistoryResult>) request.getSession().getAttribute("revisions");
        HistoryResult result = results.get(index);
        boolean verified = licenceQLDBRepository.verifyRevision(result.getDocumentId(), result.getBlockAddress(), result.getIntegrityInfo().getHash());
        log.info("Verified licence " + verified);
        return verified;
    }

    /**
     * Ajax Post
     * @return
     */
    @GetMapping("/licenceActivity")
    public ModelAndView queryLicenceActivity(HttpServletRequest request, @RequestParam  String id, Model model) throws JsonProcessingException {
        Optional<LicenceActivity> licenceActivity = licenceActivityDynamoRepository.findById(id);

        if(licenceActivity == null) {
            model.addAttribute("historyNotAvailableMessage", "Data is not available in DynamoDB");
            return new ModelAndView("licenceMgmt :: licenceActivityNotAvailable");
        }

        if(licenceActivity.isPresent()) {
            LicenceActivity activity = licenceActivity.get();

            int penaltyPointsAddedEvents = 0;

            int penaltyPointsRemovedEvents = 0;

            int nameChangedEvents = 0;

            int contactDetailsChangedEvents = 0;

            for(Activity activityEvent : activity.getActivity()) {
                if(activityEvent.getEventName().equalsIgnoreCase("PenaltyPointsAdded")) {
                    penaltyPointsAddedEvents = penaltyPointsAddedEvents + 1;
                } else if(activityEvent.getEventName().equalsIgnoreCase("PenaltyPointsRemoved")) {
                    penaltyPointsRemovedEvents = penaltyPointsRemovedEvents + 1;
                } else if(activityEvent.getEventName().equalsIgnoreCase("ContactDetailsChanged")) {
                    nameChangedEvents = nameChangedEvents + 1;
                } else if(activityEvent.getEventName().equalsIgnoreCase("NameChanged")) {
                    contactDetailsChangedEvents = contactDetailsChangedEvents + 1;
                }
            }
            activity.setContactDetailsChangedEvents(contactDetailsChangedEvents);
            activity.setNameChangedEvents(nameChangedEvents);
            activity.setPenaltyPointsAddedEvents(penaltyPointsAddedEvents);
            activity.setPenaltyPointsRemovedEvents(penaltyPointsRemovedEvents);
            model.addAttribute("licenceActivity", activity);
        }


        return new ModelAndView("licenceMgmt :: licenceActivityFragment");
    }
}
