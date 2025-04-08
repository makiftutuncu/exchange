package dev.akif.exchange.rate.impl;

import dev.akif.exchange.common.Controller;
import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Rate Controller",
    description = "This controller contains endpoints about conversion rates between currencies.")
@RestController
@RequestMapping("/rates")
public class RateController extends Controller {
  private final RateService rateService;

  @Autowired
  public RateController(RateService rateService) {
    this.rateService = rateService;
  }

  @Operation(
      summary = "Get rate for a currency pair",
      description =
          "This endpoint can be used to get a conversion rate between a source and a target currency.",
      parameters = {
        @Parameter(
            name = "source",
            in = ParameterIn.QUERY,
            description = "Source currency of the conversion rate",
            required = true),
        @Parameter(
            name = "target",
            in = ParameterIn.QUERY,
            description = "Target currency of the conversion rate",
            required = true)
      },
      responses = {
        @ApiResponse(
            description = "Successful response",
            responseCode = "200",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = RateResponse.class),
                  examples = {
                    @ExampleObject(value = "{\"source\":\"USD\",\"target\":\"TRY\",\"rate\":7.0}")
                  })
            }),
        @ApiResponse(
            description = "Input is invalid",
            responseCode = "400",
            content = {
              @Content(
                  mediaType = "application/json",
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"code\":400,\"name\":\"invalid-input\",\"message\":\"Currency is invalid\"}")
                  })
            }),
        @ApiResponse(
            description = "Cannot read rate",
            responseCode = "500",
            content = {
              @Content(
                  mediaType = "application/json",
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"code\":500,\"name\":\"database\",\"message\":\"Cannot read rate\"}")
                  })
            }),
        @ApiResponse(
            description = "Cannot get latest rates",
            responseCode = "503",
            content = {
              @Content(
                  mediaType = "application/json",
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"code\":503,\"name\":\"service-unavailable\",\"message\":\"Cannot get rates from fixer.io\"}")
                  })
            })
      })
  @GetMapping
  public ResponseEntity<RateResponse> rates(
      @RequestParam("source") String source, @RequestParam("target") String target) {
    return respond(() -> rateService.rate(CurrencyPair.of(source, target)));
  }
}
