"use client";

export type Currency = {
  code: string;
  name: string;
  symbol: string;
  symbolIsPrefixed: boolean;
};

export type CurrenciesProps = {
  currencies: Currency[];
  selected?: Currency;
  label?: string;
  onSelected?: (currency: Currency | undefined) => void;
  className?: string | undefined;
};

export default function Currencies({
  currencies,
  selected,
  label,
  onSelected,
  className,
}: CurrenciesProps) {
  return (
    <div className={`format dark:format-invert ${className}`}>
      {label && <label htmlFor="currencies">{label}</label>}
      <div className="flex">
        {selected && (
          <span className="rounded-e-0 inline-flex items-center rounded-s-md border border-e-0 border-gray-300 bg-gray-200 px-3 text-sm text-gray-900 dark:border-gray-600 dark:bg-gray-600 dark:text-gray-400">
            {selected.symbol}
          </span>
        )}
        <select
          id="currencies"
          className={
            "block w-full rounded-e-lg border border-gray-300 bg-gray-50 p-2.5 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400 dark:focus:border-blue-500 dark:focus:ring-blue-500" +
            (selected
              ? ""
              : " rounded-s-lg border-s-gray-300 dark:border-s-gray-600")
          }
          onChange={(e) => {
            const newSelected = currencies.find(
              (c) => c.code === e.target.value,
            );
            if (onSelected) {
              onSelected(newSelected);
            }
          }}
          value={selected?.code || ""}
        >
          <option value="">-</option>
          {currencies.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
