"use client";

import { Button, Card, Spinner, TextInput } from "flowbite-react";
import Currencies, { Currency } from "./Currencies";
import { useEffect, useState } from "react";
import { MdSwapHoriz as SwapIcon, MdClear as ClearIcon } from "react-icons/md";
import Alert, { AlertData, buildErrorAlert } from "../common/Alert";
import { useDebouncedCallback } from "use-debounce";

type CurrentRatesState = {
  from: Currency | undefined;
  to: Currency | undefined;
  amount: number | undefined;
  convertedAmount: number | undefined;
  rate: number | undefined;
  loading: boolean;
};

export default function CurrentRates() {
  // TODO: Replace with loading currencies from API
  const currencies: Currency[] = [
    {
      code: "EUR",
      name: "Euros",
      symbol: "€",
      symbolIsPrefixed: true,
    },
    {
      code: "USD",
      name: "United States Dollars",
      symbol: "$",
      symbolIsPrefixed: true,
    },
    {
      code: "TRY",
      name: "Turkish Liras",
      symbol: "₺",
      symbolIsPrefixed: false,
    },
  ];

  const [state, setState] = useState<CurrentRatesState>({
    from: undefined,
    to: undefined,
    amount: undefined,
    convertedAmount: undefined,
    rate: undefined,
    loading: false,
  });

  const [alertData, setAlertData] = useState<AlertData>({ message: "" });

  const getRate = async (from: Currency, to: Currency) => {
    try {
      setState((oldState) => ({ ...oldState, loading: true }));
      const data = await fetch(
        `http://localhost:8080/rates?source=${from.code}&target=${to.code}`,
      ).then((response) => response.json());
      const rate: number = data.rate;
      setState((oldState) => ({ ...oldState, rate: rate, loading: false }));
    } catch (e) {
      const message = "Cannot get rate from " + from.code + " to " + to.code;
      console.log(message, e);
      setAlertData(buildErrorAlert(message));
      setState((oldState) => ({
        ...oldState,
        rate: undefined,
        loading: false,
      }));
    }
  };

  const convert = async (from: Currency, to: Currency, amount: number) => {
    try {
      setState((oldState) => ({ ...oldState, loading: true }));
      const data = await fetch(`http://localhost:8080/conversions`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          source: from.code,
          target: to.code,
          amount: amount,
        }),
      }).then((response) => response.json());
      setState((oldState) => ({
        ...oldState,
        amount: amount,
        convertedAmount: data.targetAmount,
        loading: false,
      }));
    } catch (e) {
      const message = "Cannot convert from " + from.code + " to " + to.code;
      console.log(message, e);
      setAlertData(buildErrorAlert(message));
      setState((oldState) => ({
        ...oldState,
        amount: undefined,
        convertedAmount: undefined,
        loading: false,
      }));
    }
  };

  const amount = (value: number, currency: Currency): string => {
    const prefix = currency.symbolIsPrefixed ? currency.symbol : "";
    const suffix = currency.symbolIsPrefixed ? "" : currency.symbol;
    return prefix + value + suffix;
  };

  const debouncedConvert = useDebouncedCallback(convert, 500);

  useEffect(() => {
    if (!state.from || !state.to) {
      return;
    }

    getRate(state.from, state.to).then(() => {
      if (state.amount) {
        convert(state.from!, state.to!, state.amount);
      }
    });
  }, [state.from, state.to, state.amount]);

  return (
    <Card className="format dark:format-invert mx-4 justify-center sm:mx-8">
      <div className="flex flex-row justify-between">
        <h1 className="my-0 text-2xl">Current Rates</h1>
        <Button
          color="alternative"
          type="reset"
          disabled={
            state.loading ||
            (state.from === undefined &&
              state.to === undefined &&
              state.rate === undefined &&
              state.amount === undefined &&
              state.convertedAmount === undefined)
          }
          onClick={() =>
            setState((oldState) => ({
              ...oldState,
              from: undefined,
              to: undefined,
              amount: undefined,
              convertedAmount: undefined,
              rate: undefined,
            }))
          }
        >
          <ClearIcon />
        </Button>
      </div>
      <div className="flex flex-row items-end gap-2">
        <Currencies
          label="From"
          currencies={currencies}
          selected={state.from}
          onSelected={(from) => {
            setState((oldState) => ({
              ...oldState,
              from: from,
            }));
          }}
        />
        <Button
          color="dark"
          onClick={() =>
            setState((oldState) => ({
              ...oldState,
              from: oldState.to,
              to: oldState.from,
            }))
          }
        >
          <SwapIcon />
        </Button>
        <Currencies
          label="To"
          currencies={currencies}
          selected={state.to}
          onSelected={(to) => {
            setState((oldState) => ({
              ...oldState,
              to: to,
            }));
          }}
        />
      </div>
      {state.from && state.to && (
        <TextInput
          placeholder="Custom amount to convert"
          addon={state.from.symbol}
          type="number"
          defaultValue={state.amount}
          onChange={(e) => {
            const newAmount = Number.parseFloat(e.target.value);
            if (Number.isNaN(newAmount)) {
              setState((oldState) => ({
                ...oldState,
                amount: undefined,
                convertedAmount: undefined,
              }));
            } else {
              debouncedConvert(state.from!, state.to!, newAmount);
            }
          }}
        />
      )}
      <div className="flex flex-row items-center justify-center">
        {state.loading && (
          <Spinner aria-label="Loading" size="xl" color="gray" />
        )}
        {state.from && state.to && !state.loading && state.rate && (
          <h2 className="my-0 text-2xl font-normal">
            {amount(state.amount || 1, state.from)} ={" "}
            {amount(state.convertedAmount || state.rate, state.to)}
          </h2>
        )}
      </div>
      <Alert data={alertData} setData={setAlertData} />
    </Card>
  );
}
