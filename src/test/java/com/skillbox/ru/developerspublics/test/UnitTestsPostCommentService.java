package com.skillbox.ru.developerspublics.test;

import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiComment;
import main.com.skillbox.ru.developerspublics.api.response.ErrorsResponse;
import main.com.skillbox.ru.developerspublics.api.response.ResultFalseErrorsResponse;
import main.com.skillbox.ru.developerspublics.model.entity.Post;
import main.com.skillbox.ru.developerspublics.model.entity.PostComment;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.enums.ModerationStatuses;
import main.com.skillbox.ru.developerspublics.model.repository.PostsRepository;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import main.com.skillbox.ru.developerspublics.service.PostCommentService;
import main.com.skillbox.ru.developerspublics.service.PostService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsPostCommentService {

    private PostCommentService postCommentService;
    private PostService postService;
    private PostsRepository postsRepository;
    private UsersRepository usersRepository;
    private UserService userService;
    private PostComment comment;
    private Post post;
    private User user;
    private final String commentText = "comment text to test comment";

    @Autowired
    public void UnitTestsPostVoteService(
        PostCommentService postCommentService,
        PostService postService,
        UserService userService,
        PostsRepository postsRepository,
        UsersRepository usersRepository) {
        this.postCommentService = postCommentService;
        this.postService = postService;
        this.userService = userService;
        this.postsRepository = postsRepository;
        this.usersRepository = usersRepository;
    }

    private void initPost() {
        String title = "testTitle testTitle testTitle";
        post = postsRepository.findByTitle(title);
        if (post != null) postService.deletePost(post);

        String email = "testEmail";
        String password = "testPassword";
        user = usersRepository.findUserByEmail(email);
        if (user != null) userService.deleteUser(user);
        user = new User(email, "testName", password);
        usersRepository.save(user);
        userService.authUser(email, password);

        post = new Post();
        post.setIsActive(1);
        post.setModerationStatus(ModerationStatuses.ACCEPTED.toString());
        post.setUserId(user.getId());
        post.setTime(System.currentTimeMillis());
        post.setTitle(title);
        post.setText("testText testText testText testText testText testText testText testText testText testText");
        post.setViewCount(0);
        postsRepository.save(post);
        post = postsRepository.findByTitle(post.getTitle());
    }

    private void clearAll() {
        SecurityContextHolder.clearContext();
        postService.deletePost(post);
        userService.deleteUser(user);
    }

    @Test
    @Transactional
    public void testPostApiCommentNonAuth() {
        SecurityContextHolder.getContext().setAuthentication(null);

        RequestApiComment request = new RequestApiComment(null, 0, commentText);

        ResponseEntity<?> response = postCommentService.postApiComment(request);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assert.assertNull(response.getBody());
    }

    @Test
    @Transactional
    public void testPostApiCommentBadParentId() {
        initPost();

        RequestApiComment request = new RequestApiComment(0, post.getId(), commentText);

        ResponseEntity<?> response = postCommentService.postApiComment(request);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNull(response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiCommentBadPostId() {
        initPost();

        RequestApiComment request = new RequestApiComment(null, 0, commentText);

        ResponseEntity<?> response = postCommentService.postApiComment(request);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNull(response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiCommentBadText() {
        initPost();

        RequestApiComment request = new RequestApiComment(null, post.getId(), "1");

        ResponseEntity<?> response = postCommentService.postApiComment(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertEquals(new ResultFalseErrorsResponse(new ErrorsResponse("error")), response.getBody());

        clearAll();
    }

    @Test
    @Transactional
    public void testPostApiCommentOK() {
        initPost();

        RequestApiComment request = new RequestApiComment(null, post.getId(), commentText);

        ResponseEntity<?> response = postCommentService.postApiComment(request);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assert.assertNotEquals(0, response.getBody());

        clearAll();
    }
}