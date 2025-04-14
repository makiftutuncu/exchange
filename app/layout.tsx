import { ThemeModeScript } from "flowbite-react";
import type { Metadata } from "next";
import { Roboto } from "next/font/google";
import "./globals.css";
import { FooterCopyright } from "flowbite-react";
import Header from "./common/Header";

const roboto = Roboto({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Exchange",
  description: "See exchange rates and make conversions",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <ThemeModeScript />
      </head>
      <body
        className={`${roboto.className} flex h-screen flex-col bg-gray-100 antialiased dark:bg-gray-800`}
      >
        <Header />
        <main className="my-auto flex flex-col items-center">{children}</main>
        <FooterCopyright
          href="https://akif.dev"
          by="Mehmet Akif Tütüncü"
          year={new Date().getFullYear()}
          className="my-2 text-center"
        />
      </body>
    </html>
  );
}
