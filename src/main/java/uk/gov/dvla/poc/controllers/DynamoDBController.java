package uk.gov.dvla.poc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.dvla.poc.events.NameChanged;
import uk.gov.dvla.poc.events.PenaltyPointsAdded;
import uk.gov.dvla.poc.events.PenaltyPointsRemoved;
import uk.gov.dvla.poc.model.*;
import uk.gov.dvla.poc.repository.BicycleLicenceDynamoDBRepository;
import uk.gov.dvla.poc.repository.LicenceActivityDynamoRepository;
import org.springframework.ui.Model;
import uk.gov.dvla.poc.forms.BicycleLicenceQueryForm;
import uk.gov.dvla.poc.forms.BicycleLicenceUpdateForm;
import uk.gov.dvla.poc.model.dynamo.Activity;
import uk.gov.dvla.poc.model.dynamo.LicenceActivity;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@Slf4j
public class DynamoDBController {

    private final BicycleLicenceDynamoDBRepository licenceDynamoDBRepository;
    private final LicenceActivityDynamoRepository licenceActivityDynamoRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public DynamoDBController(BicycleLicenceDynamoDBRepository licenceDynamoDBRepository, LicenceActivityDynamoRepository licenceActivityDynamoRepository) {
        this.licenceDynamoDBRepository = licenceDynamoDBRepository;
        this.licenceActivityDynamoRepository = licenceActivityDynamoRepository;
    }

    @PostMapping("/queryLicence")
    public String queryLicence(@ModelAttribute BicycleLicenceQueryForm form, Model model) throws JsonProcessingException {
        log.info("Querying licence");
        BicycleLicence licence = licenceDynamoDBRepository.findByEmail(form.getEmail());
        if(licence != null) {
            model.addAttribute("licence", licence);
            return mapper.writeValueAsString(licence);
        }
        return null;
    }

    @PostMapping("/updateLicenceForm")
    public String updateLicenceForm(HttpServletRequest request, @ModelAttribute BicycleLicenceUpdateForm form, Model model) {
        log.info("Updating licence");
        BicycleLicence licence = licenceDynamoDBRepository.findByEmail(form.getEmail());
        if(licence != null) {
            log.info("The licence penalty points is " + licence.getPenaltyPoints());
            log.info("The form penalty points is " + form.getPenaltyPoints());

            if (form.getPenaltyPoints() > 0) {
                licence.addEvent(new PenaltyPointsAdded(form.getPenaltyPoints()));
                int points  = licence.getPenaltyPoints() + form.getPenaltyPoints();
                licence.setPenaltyPoints(points);
            } else if (form.getPenaltyPoints() < 0) {
                licence.addEvent(new PenaltyPointsRemoved(form.getPenaltyPoints()));
                int points  = licence.getPenaltyPoints() + form.getPenaltyPoints();
                licence.setPenaltyPoints(points);
            } else if (form.getPenaltyPoints() == 0) {
                if (!form.getName().equals(licence.getName())) {
                    licence.addEvent(new NameChanged());
                    licence.setName(form.getName());
                }
            }
            licence = licenceDynamoDBRepository.save(licence);
            log.info("Licence updated");
            return "success";
        }
        return "error";
    }

    @GetMapping("/licenceActivity")
    public ModelAndView queryLicenceActivity(HttpServletRequest request, @RequestParam String id, Model model) throws JsonProcessingException {
        log.info("Querying licence activity");
        Optional<LicenceActivity> activities = licenceActivityDynamoRepository.findById(id);
        model.addAttribute("activities", activities);
        return new ModelAndView("activity");
    }

    /**
     * Ajax Post
     * @return
     */
    @GetMapping("/history")
    public ModelAndView queryHistory(HttpServletRequest request, @RequestParam String email, @RequestParam String docId, Model model) throws JsonProcessingException {
        log.info("Email " + email);
        log.info("Doc Id " + docId);

        BicycleLicence licence = licenceDynamoDBRepository.findByEmail(email);
        log.info("Licence " + licence);

        List<BicycleLicence> licences = new ArrayList<>();
        licences.add(licence);

        model.addAttribute("revisions", licences);
        request.getSession().setAttribute("revisions", licences);

        return new ModelAndView("licenceMgmt :: historyResultsList");
    }


}