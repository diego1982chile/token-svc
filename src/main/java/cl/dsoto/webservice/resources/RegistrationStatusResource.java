package cl.dsoto.webservice.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationStatusResource {

    private boolean confirmed;
    private OnboardingTrainResource train;
}
