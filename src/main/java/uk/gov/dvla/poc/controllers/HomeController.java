package uk.gov.dvla.poc.controllers;

import com.amazon.ion.IonReader;
import com.amazon.ion.system.IonReaderBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.dvla.poc.forms.*;
import uk.gov.dvla.poc.model.BicycleLicence;
import uk.gov.dvla.poc.repository.BicycleLicenceQLDBRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Controller
public class HomeController {

    private BicycleLicenceQLDBRepository licenceQLDBRepository;

    public HomeController(BicycleLicenceQLDBRepository licenceQLDBRepository) {
        this.licenceQLDBRepository = licenceQLDBRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "home";
    }

    @GetMapping("/licenceMgmt")
    public String createLicenceForm(HttpServletRequest request, Model model) {
        addLicencesToModel(request, model);

        initForms(model);
        return "licenceMgmt";
    }

    @PostMapping("/submitLicenceForm")
    public String submitLicenceForm(HttpServletRequest request, @ModelAttribute BicycleLicenceCreateForm form, Model model) {
        Iterable<BicycleLicence> licences = addLicencesToModel(request, model);
        BicycleLicence existingLicence = StreamSupport.stream(licences.spliterator(), false)
                .filter(licence -> form.getEmail().equals(licence.getEmail()))
                .findAny()
                .orElse(null);

        if(existingLicence != null) {
            model.addAttribute("message", "Email already exists - please try again");
            initForms(model);
            return "licenceMgmt";
        }
        BicycleLicence licence = new BicycleLicence();
        licence.setId(UUID.randomUUID().toString());
        licence.setEmail(form.getEmail());
        licence.setName(form.getName());
        licence.setTelephone(form.getTelephone());
        licence.setPenaltyPoints(0);
        BicycleLicence committedLicence = licenceQLDBRepository.save(licence);
        model.addAttribute("message", "Bicycle Licence Added: Doc Id(" + committedLicence.getId() + ")");
        addLicencesToModel(request, model);

        return "licenceMgmt";
    }



    private void initForms(Model model) {
        BicycleLicenceCreateForm form = new BicycleLicenceCreateForm();
        model.addAttribute("bicycleLicenceCreateForm", form);

        BicycleLicenceQueryForm queryForm = new BicycleLicenceQueryForm();
        model.addAttribute("bicycleLicenceQueryForm", queryForm);

        BicycleLicenceUpdateForm licenceUpdateForm = new BicycleLicenceUpdateForm();
        model.addAttribute("bicycleLicenceUpdateForm", licenceUpdateForm);

        VerifyLicenceForm verifyLicenceForm = new VerifyLicenceForm();
        model.addAttribute("verifyLicenceForm", verifyLicenceForm);

        LicenceIdQueryForm licenceActivityQueryForm = new LicenceIdQueryForm();
        model.addAttribute("licenceActivityQueryForm", licenceActivityQueryForm);
    }

    private Iterable<BicycleLicence> addLicencesToModel(HttpServletRequest request, Model model) {
        Iterable<BicycleLicence> licences = licenceQLDBRepository.findAll();
        model.addAttribute("licences", licences);
        request.getSession().setAttribute("licences", licences);
        return licences;
    }
}