package dev.akif.exchange.conversion;

public record ConversionResponse(
    Long id,
    String source,
    double sourceAmount,
    String target,
    double targetAmount,
    double rate,
    long createdAt) {
  public ConversionResponse(Long id, Conversion conversion) {
    this(
        id,
        conversion.getSource(),
        conversion.getSourceAmount(),
        conversion.getTarget(),
        conversion.getTargetAmount(),
        conversion.getRate(),
        conversion.getCreatedAt());
  }

  public ConversionResponse(Conversion conversion) {
    this(conversion.getId(), conversion);
  }
}
