"use client";

import { Button, Card, Spinner } from "flowbite-react";
import Currencies, { Currency } from "./Currencies";
import { useEffect, useState } from "react";
import { MdSwapHoriz as SwapIcon } from "react-icons/md";
import Alert, { AlertData, buildErrorAlert } from "./Alert";

type GetRateState = {
  currencies: Currency[];
  from: Currency | undefined;
  to: Currency | undefined;
  rate: number | undefined;
  rateLoading: boolean;
};

export default function Home() {
  const [state, setState] = useState<GetRateState>({
    from: undefined,
    to: undefined,
    // TODO: Replace with loading currencies from API
    currencies: [
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
    ],
    rate: undefined,
    rateLoading: false,
  });

  const [alertData, setAlertData] = useState<AlertData>({ message: "" });

  const getRate = async (from: Currency, to: Currency) => {
    try {
      setState((oldState) => ({ ...oldState, rateLoading: true }));
      const data = await fetch(
        `http://localhost:8080/rates?source=${from.code}&target=${to.code}`,
      ).then((response) => response.json());
      const rate: number = data.rate;
      setState((oldState) => ({ ...oldState, rate: rate, rateLoading: false }));
    } catch (e) {
      const message = "Cannot get rate from " + from.code + " to " + to.code;
      console.log(message, e);
      setAlertData(buildErrorAlert(message));
      setState((oldState) => ({
        ...oldState,
        rate: undefined,
        rateLoading: false,
      }));
    }
  };

  const amount = (value: number, currency: Currency): string => {
    const prefix = currency.symbolIsPrefixed ? currency.symbol : "";
    const suffix = currency.symbolIsPrefixed ? "" : currency.symbol;
    return prefix + value + suffix;
  };

  useEffect(() => {
    if (!state.from || !state.to) {
      return;
    }

    getRate(state.from, state.to);
  }, [state.from, state.to]);

  return (
    <Card className="format dark:format-invert mx-4 justify-center sm:mx-8">
      <h1 className="my-0 text-2xl">Get Rate</h1>
      <div className="flex flex-row items-end gap-2">
        <Currencies
          label="From"
          currencies={state.currencies}
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
          currencies={state.currencies}
          selected={state.to}
          onSelected={(to) => {
            setState((oldState) => ({
              ...oldState,
              to: to,
            }));
          }}
        />
      </div>
      <div className="flex flex-row items-center justify-center">
        {state.rateLoading && (
          <Spinner aria-label="Loading" size="xl" color="gray" />
        )}
        {state.from && state.to && !state.rateLoading && state.rate && (
          <h2 className="my-0 text-2xl font-normal">
            {amount(1, state.from)} = {amount(state.rate, state.to)}
          </h2>
        )}
      </div>
      <Alert data={alertData} setData={setAlertData} />
    </Card>
  );
}
