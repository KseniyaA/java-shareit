package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(UserDtoRequest userDtoRequest) {
        return post("", userDtoRequest);
    }

    public ResponseEntity<Object> update(UserDtoRequest userDtoRequest, long userId) {
        Map<String, Object> parameters = Map.of(
                "userId", userId
        );
        return patch("/{userId}", userId, parameters, userDtoRequest);
    }

    public ResponseEntity<Object> deleteById(long userId) {
        Map<String, Object> parameters = Map.of(
                "userId", userId
        );
        return delete("/{userId}", userId, parameters);
    }

    public ResponseEntity<Object> getById(long id) {
        Map<String, Object> parameters = Map.of(
                "userId", id
        );
        return get("/{userId}", id, parameters);
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }
}
