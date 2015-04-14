package it.dupps.communication;

import lombok.*;

import java.util.UUID;

/**
 * Created by dupps on 13.04.15.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class Communication {

    @NonNull
    private ComType type;

    private String username;

    private String password;

    private UUID token;

    private Integer amount;

    private String payload;

}
