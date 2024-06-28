package com.edilson.tweet.service;

import com.edilson.tweet.dto.CreateTweetDto;
import com.edilson.tweet.dto.FeedDto;
import com.edilson.tweet.dto.FeedItemDto;
import com.edilson.tweet.entitie.Role;
import com.edilson.tweet.entitie.Tweet;
import com.edilson.tweet.repository.TweetRepository;
import com.edilson.tweet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public FeedDto feed(int page, int pageSize) {
        var tweets = tweetRepository.findAll(
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet -> new FeedItemDto(tweet.getId(), tweet.getContent(), tweet.getUser().getUsername())
                );
        return new FeedDto(tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements());
    }

    public boolean createTweet(CreateTweetDto createTweetDto, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));
        if(user.isPresent()) {
            var tweet = new Tweet();
            tweet.setUser(user.get());
            tweet.setContent(createTweetDto.content());
            tweetRepository.save(tweet);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean deleteTweet(Long tweetId, JwtAuthenticationToken token) {
        var user = userRepository.findById(UUID.fromString(token.getName()));
        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getId().equals(UUID.fromString(token.getName()))) {
            tweetRepository.deleteById(tweetId);
            return true;
        }
        else {
            return false;
        }
    }
}
