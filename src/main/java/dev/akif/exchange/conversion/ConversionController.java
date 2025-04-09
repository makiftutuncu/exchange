package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Conversion Controller",
    description = "This controller contains endpoints about conversions between currencies.")
@RestController
@RequestMapping("/conversions")
public class ConversionController {
  private final ConversionService conversionService;

  @Autowired
  public ConversionController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Operation(
      summary = "Make a new conversion",
      description =
          "This endpoint can be used to make a conversion between a source and a target currency for given amount.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Conversion request",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ConversionRequest.class),
                      examples = {
                        @ExampleObject(
                            value = "{\"source\":\"USD\",\"target\":\"TRY\",\"amount\":10.0}")
                      }),
              required = true),
      responses = {
        @ApiResponse(
            description = "Successful response",
            responseCode = "201",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ConversionResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"id\":1,\"source\":\"USD\",\"sourceAmount\":10.0,\"target\":\"TRY\",\"targetAmount\":70.0,\"rate\":7.0,\"createdAt\":123456789}")
                  })
            }),
        @ApiResponse(
            description = "Input is invalid",
            responseCode = "400",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Currency is invalid")})
            }),
        @ApiResponse(
            description = "Cannot read rate",
            responseCode = "500",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Cannot read rate")})
            }),
        @ApiResponse(
            description = "Cannot save conversion",
            responseCode = "500",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Cannot save conversion")})
            }),
        @ApiResponse(
            description = "Cannot get latest rates",
            responseCode = "503",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Cannot get rates from fixer.io")})
            })
      })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConversionResponse convert(@RequestBody ConversionRequest request) {
    CurrencyPair pair = CurrencyPair.of(request.source(), request.target());
    return conversionService.convert(pair, request.amount());
  }

  @Operation(
      summary = "Get a conversion",
      description = "This endpoint can be used to get an existing conversion by its id.",
      parameters = {
        @Parameter(
            name = "id",
            in = ParameterIn.PATH,
            description = "Id of the conversion",
            required = true)
      },
      responses = {
        @ApiResponse(
            description = "Successful response",
            responseCode = "200",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ConversionResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"id\":1,\"source\":\"USD\",\"sourceAmount\":10.0,\"target\":\"TRY\",\"targetAmount\":70.0,\"rate\":7.0,\"createdAt\":123456789}")
                  })
            }),
        @ApiResponse(
            description = "Conversion not found",
            responseCode = "404",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Conversion not found")})
            }),
        @ApiResponse(
            description = "Cannot read conversion",
            responseCode = "500",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Cannot read conversion")})
            })
      })
  @GetMapping("/{id}")
  public ConversionResponse get(@PathVariable long id) {
    return conversionService.get(id);
  }

  @Operation(
      summary = "List conversions",
      description = "This endpoint can be used to get and filter existing conversions.",
      parameters = {
        @Parameter(
            name = "from",
            in = ParameterIn.QUERY,
            description = "Conversions created after (including this date)"),
        @Parameter(
            name = "to",
            in = ParameterIn.QUERY,
            description = "Conversions created before (excluding this date)"),
        @Parameter(
            name = "page",
            in = ParameterIn.QUERY,
            description = "Page number (starting from 1)"),
        @Parameter(
            name = "size",
            in = ParameterIn.QUERY,
            description = "Number of conversions in a page (between 1 and 50, inclusive)"),
        @Parameter(
            name = "newestFirst",
            in = ParameterIn.QUERY,
            description = "Whether to list newest conversions first")
      },
      responses = {
        @ApiResponse(
            description = "Successful response",
            responseCode = "200",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ConversionResponse.class),
                  examples = {
                    @ExampleObject(
                        value =
                            "{\"data\":[{\"id\":1,\"source\":\"USD\",\"sourceAmount\":10.0,\"target\":\"TRY\",\"targetAmount\":70.0,\"rate\":7.0,\"createdAt\":123456789}],\"page\":1,\"totalPages\":2,\"pageSize\":1,\"totalSize\":2}")
                  })
            }),
        @ApiResponse(
            description = "Cannot read conversion",
            responseCode = "500",
            content = {
              @Content(
                  mediaType = "text/plain",
                  examples = {@ExampleObject(value = "Cannot read conversion")})
            })
      })
  @GetMapping
  public PagedResponse<ConversionResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false, defaultValue = "1") int page,
      @RequestParam(required = false, defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "true") boolean newestFirst) {
    return conversionService.list(from, to, page, size, newestFirst);
  }
}
