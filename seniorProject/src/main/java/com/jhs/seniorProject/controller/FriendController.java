package com.jhs.seniorProject.controller;

import com.jhs.seniorProject.argumentresolver.Login;
import com.jhs.seniorProject.controller.form.FriendForm;
import com.jhs.seniorProject.domain.Friend;
import com.jhs.seniorProject.domain.User;
import com.jhs.seniorProject.domain.exception.DuplicateFriendException;
import com.jhs.seniorProject.domain.exception.NoSuchUserException;
import com.jhs.seniorProject.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Slf4j
@Controller
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/list")
    public String getFriends(@Login User user, @ModelAttribute("user") FriendForm friendForm, Model model) {
        //TODO Entity -> DTO 변경
        List<String> result = friendService.getFriends(user).stream()
                .map(FriendController::apply)
                .collect(toList());
        model.addAttribute("friends", result);
        return "friend/friendList";
    }

    @ResponseBody
    @PostMapping("/api/add")
    public void addFriend(@Valid @RequestBody FriendForm friendForm, BindingResult bindingResult, @Login User user) throws BindException {
        log.info("add friend {}", friendForm);
        if (bindingResult.hasErrors()) {
            log.info("bindingResult has error, ", bindingResult);
            //TODO ControllerAdvice 만들어서 에러 메세지 관리
            throw new BindException(bindingResult);
        }
        //TODO 예외처리 로직 기능 추가
        //TODO Entity -> DTO 변경
        try {
            friendService.addFriend(user, friendForm.getId());
        } catch (DuplicateFriendException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }
    }

    private static String apply(Friend friend) {
        return friend.getFriendId().getName();
    }
}
