package com.armensokhakyan.main.services;

import com.armensokhakyan.main.dto.PostDTO;
import com.armensokhakyan.main.entity.ImageModel;
import com.armensokhakyan.main.entity.Post;
import com.armensokhakyan.main.entity.User;
import com.armensokhakyan.main.exceptions.PostNotFoundException;
import com.armensokhakyan.main.repository.ImageRepository;
import com.armensokhakyan.main.repository.PostRepository;
import com.armensokhakyan.main.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    public static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, ImageRepository imageRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    public Post createPost(PostDTO postDTO, Principal principal) {
        User user = getUserByPrincipal(principal);
        Post post = new Post();
        post.setUser(post.getUser());
        post.setCaption(post.getCaption());
        post.setLocation(post.getLocation());
        post.setTitle(post.getTitle());
        post.setLikes(0);

        LOG.info("Saving Post for User: {}", user.getEmail());
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedDateDesc();
    }

    public Post getPostById(Long postId, Principal principal) {
        User user = getUserByPrincipal(principal);
        return postRepository.findPostByIdAndUser(postId, user)
                .orElseThrow(() -> new PostNotFoundException("Post cannot be found for username: " + user.getEmail()));
    }

    public List<Post> getAllPostsForUser(Principal principal) {
        User user = getUserByPrincipal(principal);
        return postRepository.findAllByUserOrderByCreatedDateDesc(user);

    }

    public Post likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));

        Optional<String> userLiked = post.getLikedUsers()
                .stream().filter(u -> u.equals(username)).findAny();

        if (userLiked.isPresent()) {
            post.setLikes(post.getLikes() + 1);
            post.getLikedUsers().add(username);
        } else {
            post.setLikes(post.getLikes() - 1);
            post.getLikedUsers().remove(username);
        }

        return postRepository.save(post);
    }

    public void deletePost(Long postId, Principal principal) {
        User user = getUserByPrincipal(principal);
        Post post = postRepository.findPostByIdAndUser(postId, user)
                .orElseThrow(()-> new PostNotFoundException("Post not found for username: " + user.getEmail()));
        Optional<ImageModel> imageModel = imageRepository.findByPostId(post.getId());
        imageModel.ifPresent(imageRepository::delete);
        postRepository.delete(post);
    }

    private User getUserByPrincipal(Principal principal) {
        String username = principal.getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by username " + username));
    }
}
