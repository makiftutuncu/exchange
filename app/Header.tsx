"use client";

import { DarkThemeToggle } from "flowbite-react";
import "./globals.css";
import {
  Dropdown,
  DropdownDivider,
  DropdownHeader,
  DropdownItem,
  Navbar,
  NavbarBrand,
} from "flowbite-react";
import {
  MdPerson as PersonIcon,
  MdLogin as LoginIcon,
  MdLogout as LogoutIcon,
} from "react-icons/md";
import { useState } from "react";
import Image from "next/image";

type User = {
  name: string;
  email: string;
  photo: string;
};

export default function Header() {
  const [user, setUser] = useState<User>();

  // TODO: Replace with real user loading logic
  const login = () =>
    setUser({
      name: "Mehmet Akif Tütüncü",
      email: "mat@akif.dev",
      photo:
        "https://gravatar.com/avatar/7874b1a2b3af81d12643f8b6d9c828f5?s=256",
    });

  const logout = () => setUser(undefined);

  return (
    <Navbar fluid className="h-16 w-full bg-gray-200 py-3 dark:bg-gray-900">
      <NavbarBrand href="/" className="ps-4 text-2xl font-bold dark:text-white">
        Exchange
      </NavbarBrand>
      <div className="flex">
        <DarkThemeToggle className="mr-2 cursor-pointer text-gray-700 hover:bg-gray-100 focus:ring-4 focus:ring-gray-300 focus:outline-none lg:mr-4 dark:text-gray-500 dark:hover:bg-gray-700 dark:focus:ring-gray-800" />
        <Dropdown
          arrowIcon={false}
          dismissOnClick={true}
          inline
          label={
            <div className="inline-flex cursor-pointer items-center rounded-lg text-gray-700 hover:bg-gray-100 focus:ring-4 focus:ring-gray-300 focus:outline-none dark:text-gray-500 dark:hover:bg-gray-700 dark:focus:ring-gray-800">
              {user ? (
                <Image
                  className="h-10 w-10 rounded-lg"
                  src={user.photo}
                  alt="User"
                  width={24}
                  height={24}
                  unoptimized
                />
              ) : (
                <PersonIcon size={24} className="m-2" />
              )}
            </div>
          }
        >
          {user ? (
            <>
              <DropdownHeader className="flex items-center">
                <PersonIcon size={16} className="mr-2" />
                <div>
                  <span className="block font-bold">{user.name}</span>
                  <span className="block">{user.email}</span>
                </div>
              </DropdownHeader>
              <DropdownDivider />
              <DropdownItem onClick={logout}>
                <LogoutIcon size={16} className="mr-2" />
                Log out
              </DropdownItem>
            </>
          ) : (
            <DropdownItem onClick={login}>
              <LoginIcon size={16} className="mr-2" />
              Log in
            </DropdownItem>
          )}
        </Dropdown>
      </div>
    </Navbar>
  );
}
