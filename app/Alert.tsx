"use client";

import { Alert as FlowbiteAlert } from "flowbite-react";
import { DynamicStringEnumKeysOf, FlowbiteColors } from "flowbite-react/types";
import { Dispatch, JSX, SetStateAction } from "react";
import { MdWarning as WarningIcon } from "react-icons/md";

type AlertProps = {
  data: AlertData;
  setData: Dispatch<SetStateAction<AlertData>>;
};

export type AlertData = {
  icon?: JSX.Element;
  color?: DynamicStringEnumKeysOf<FlowbiteColors>;
  message: string;
};

export const buildErrorAlert = (message: string): AlertData => ({
  icon: <WarningIcon className="me-2" />,
  color: "failure",
  message: message,
});

export default function Alert({ data, setData }: AlertProps) {
  return data.message !== "" ? (
    <FlowbiteAlert
      color={data.color}
      icon={() => data.icon}
      onDismiss={() => setData({ message: "" })}
      rounded
    >
      {data.message}
    </FlowbiteAlert>
  ) : (
    <></>
  );
}
