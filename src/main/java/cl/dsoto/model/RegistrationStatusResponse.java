package cl.dsoto.model;

import cl.dsoto.resources.dto.OnboardingTrainView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationStatusResponse {

    private boolean confirmed;
    private OnboardingTrainView train;
}
