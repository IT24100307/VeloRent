package Group2.Car.Rental.System.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VehiclePackageRequest {
    private String packageName;
    private BigDecimal price;
    private Integer duration; // days
    private String imageUrl;
    private String status; // optional, defaults to Activated
    private List<Integer> vehicleIds;
}

