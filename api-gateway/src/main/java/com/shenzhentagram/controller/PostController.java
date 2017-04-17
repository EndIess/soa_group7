package com.shenzhentagram.controller;

import com.shenzhentagram.model.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@CrossOrigin
@RestController
@RequestMapping(path = "/posts", produces = {MediaType.APPLICATION_JSON_VALUE})
public class PostController extends TemplateRestController {

    @Autowired
    private UserController userController;

    public PostController(Environment environment, RestTemplateBuilder restTemplateBuilder) {
        super(environment, restTemplateBuilder, "post");
    }

    @GetMapping()
    @ApiOperation(
            tags = "Post-API",
            value = "getPosts",
            nickname = "getPosts",
            notes = "Get all posts (Timeline)"
    )
    public ResponseEntity<PostPage> getPosts(
            Pageable pageable
    ) {
        ResponseEntity<PostPage> responseEntity = request(HttpMethod.GET, "/posts", pageable, PostPage.class);

        // Embed user into posts
        HashMap<Integer, User> cachedUsers = new HashMap<>();
        for(Post post : responseEntity.getBody().getContent()) {
            if(!cachedUsers.containsKey(post.getUserId())) {
                cachedUsers.put(post.getUserId(), userController.getUser(post.getUserId()).getBody());
            }

            post.setUser(cachedUsers.get(post.getUserId()));
        }

        return responseEntity;
    }

    @GetMapping("/{id}")
    @ApiOperation(
            tags = "Post-API",
            value = "getPost",
            nickname = "getPost",
            notes = "Get post detail by ID"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Post not found")
    })
    public ResponseEntity<Post> getPost(
            @PathVariable("id") long id
    ) {
        ResponseEntity<Post> responseEntity = request(HttpMethod.GET, "/posts/{id}", Post.class, id);

        // Embed user into post
        responseEntity.getBody().setUser(userController.getUser(responseEntity.getBody().getUserId()).getBody());

        return responseEntity;
    }

    @PostMapping()
    @ApiOperation(
            tags = "Post-API",
            value = "createPost",
            nickname = "createPost",
            notes = "Create new post"
    )
    public ResponseEntity<Post> createPost(
            @RequestBody PostCreate detail
    ) {
        detail.setUser_id(getAuthenticatedUser().getId());

        // Create post
        ResponseEntity<Post> response = request(HttpMethod.POST, "/posts", detail, Post.class);

        // Increase user post count
        // FIXME catch the exception (by now just ignored)
        try {
            userController.increasePosts((int) getAuthenticatedUser().getId());
        } catch(Exception ignored) {}

        // Embed user into post
        response.getBody().setUser(userController.getUser(response.getBody().getUserId()).getBody());

        return response;
    }

    @PatchMapping("/{id}")
    @ApiOperation(
            tags = "Post-API",
            value = "updatePost",
            nickname = "updatePost",
            notes = "Update post"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Post not found")
    })
    public ResponseEntity<Post> updatePost(
            @PathVariable("id") long id,
            @RequestBody PostUpdate detail
    ) {
        detail.setUser_id(getAuthenticatedUser().getId());

        return request(HttpMethod.PATCH, "/posts/{id}", detail, Post.class, id);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(
            tags = "Post-API",
            value = "deletePost",
            nickname = "deletePost",
            notes = "Delete post"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "Post not found")
    })
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") long id
    ) {
        // FIXME check authenticated user before delete or send auth user id to post service and let it handle itself

        // Delete post
        ResponseEntity<Void> response = request(HttpMethod.DELETE, "/posts/{id}", Void.class, id);

        // Decrease user post count
        // FIXME catch the exception (by now just ignored)
        try {
            userController.decreasePosts((int) getAuthenticatedUser().getId());
        } catch(Exception ignored) {}

        return response;
    }

    /**
     * [Internal only] Increase post comment count by one
     */
    public int increaseComments(long id) {
        return request(HttpMethod.POST, "/posts/{id}/comments/count", Post.class, id).getBody().getComments();
    }

    /**
     * [Internal only] Increase post reaction count by one
     */
    public int increaseReactions(long id) {
        return request(HttpMethod.POST, "/posts/{id}/reactions/count", Post.class, id).getBody().getReactions();
    }

    /**
     * [Internal only] Decrease post comment count by one
     */
    public int decreaseComments(long id) {
        return request(HttpMethod.PUT, "/posts/{id}/comments/count", Post.class, id).getBody().getComments();
    }

    /**
     * [Internal only] Decrease post reaction count by one
     */
    public int decreaseReactions(long id) {
        return request(HttpMethod.PUT, "/posts/{id}/reactions/count", Post.class, id).getBody().getReactions();
    }

}
