package app.websocket.entities;

import com.google.gson.annotations.Expose;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientInfo {

    @Expose
    @Getter
    private final String name;

}
