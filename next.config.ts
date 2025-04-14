import type { NextConfig } from "next";
import withFlowbiteReact from "flowbite-react/plugin/nextjs";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [{ hostname: "gravatar.com" }],
  },
};

module.exports = nextConfig;

export default withFlowbiteReact(nextConfig);
