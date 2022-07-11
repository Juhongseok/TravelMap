package com.jhs.seniorProject.controller;

import com.jhs.seniorProject.argumentresolver.Login;
import com.jhs.seniorProject.controller.form.SaveLocationForm;
import com.jhs.seniorProject.controller.form.UpdateLocationForm;
import com.jhs.seniorProject.domain.Location;
import com.jhs.seniorProject.domain.Map;
import com.jhs.seniorProject.domain.User;
import com.jhs.seniorProject.domain.enumeration.BigSubject;
import com.jhs.seniorProject.domain.exception.NoSuchMapException;
import com.jhs.seniorProject.service.LocationService;
import com.jhs.seniorProject.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;

import static com.jhs.seniorProject.domain.enumeration.BigSubject.TOGO;
import static com.jhs.seniorProject.domain.enumeration.BigSubject.WENT;

@Slf4j
@Controller
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final MapService mapService;
    private static List<BigSubject> togo;

    static{
        togo = List.of(TOGO, WENT);
    }

    @GetMapping("/{mapId}/view")
    public String Locations(@PathVariable("mapId") Long mapId) {
        return "location/locationviewmap";
    }

    @ResponseBody
    @GetMapping("/list")
    public ResponseEntity<List<Location>> getLocationList(@RequestParam Long mapId, @Login User user) {
        //TODO Entity -> DTO 변경
        log.info("mapId = {}", mapId);
        return new ResponseEntity<>(locationService.getLocations(mapId, user.getId()), HttpStatus.OK);
    }

    @GetMapping("/{mapId}/add")
    public String addLocationForm(@Login User user, @ModelAttribute(name = "saveLocationForm") SaveLocationForm saveLocationForm
            , @PathVariable Long mapId, @RequestParam Double lat, @RequestParam Double lng, @RequestParam String placeName
            , Model model) {
        log.info("lat = {}, lng = {}", lat, lng);
        try {
            //TODO DTO 하나로 통합
            Map map = mapService.getMap(mapId, user.getId());

            saveLocationForm.setLongitude(lng);
            saveLocationForm.setLatitude(lat);
            saveLocationForm.setName(placeName);

            model.addAttribute("mapId", mapId)
                    .addAttribute("bigSubjects", togo)
                    .addAttribute("smallSubjects", locationService.getSmallSubjectList(map));
        } catch (NoSuchMapException e) {
            e.printStackTrace();
        }
        return "location/addlocationform";
    }

    @PostMapping("/{mapId}/add")
    public String addLocation(@Login User user, @PathVariable Long mapId, @Validated @ModelAttribute SaveLocationForm locationForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "location/addlocationform";
        }

        log.info("saveLocationFor = {}", locationForm);
        //TODO DTO 하나로 통합
        locationService.saveLocation(locationForm, user.getId());


        redirectAttributes.addAttribute("mapId", mapId);
        return "redirect:/location/{mapId}/view";
    }

    /**
     * if 사용자가 임의의 아이디를 고의적으로 넘길경우
     * findLocation --> throw IllegalArgumentException
     * ControllerAdvice 에러 처리
     *
     * @param locationId
     * @param model
     * @return
     */
    @GetMapping("/{location}/update")
    public String modifyLocationForm(@PathVariable(name = "location") Long locationId, Model model) {
        setModelAttribute(locationId, model);
        return "location/updatelocationform";
    }

    /**
     * location 변경사항 update
     *
     * @param locationId
     * @param updateLocationForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/{location}/update")
    public String modifyLocation(@PathVariable(name = "location") Long locationId, @Validated @ModelAttribute UpdateLocationForm updateLocationForm, BindingResult bindingResult) {
        log.info("locationId = {}", locationId);
        if (bindingResult.hasErrors()) {
            return "location/updatelocationform";
        }

        locationService.updateLocation(locationId, updateLocationForm);
        return "redirect:/"; // 지도화면 만든 후 경로 변경
    }

    private void setModelAttribute(Long locationId, Model model) {
        Location findLocation = locationService.findLocation(locationId);

        model.addAttribute("locationId", locationId)
                .addAttribute("updateLocation", getUpdateLocationForm(findLocation))
                .addAttribute("smallSubjects", locationService.getSmallSubjectList(findLocation.getMap()))
                .addAttribute("bigSubjects", togo);
    }

    private UpdateLocationForm getUpdateLocationForm(Location findLocation) {
        return UpdateLocationForm.builder()
                .name(findLocation.getName())
                .bigSubject(findLocation.getBigSubject())
                .smallSubject(findLocation.getSmallSubject())
                .memo(findLocation.getMemo())
                .build();
    }

}