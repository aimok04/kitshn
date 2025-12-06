import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "kitshn.app",
  description: "Unofficial Tandoor recipes client for Android and iOS devices",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    logo: "/images/logo-lq.png",

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Docs', link: '/docs/translate' },
      { text: 'Contact', link: '/contact' }
    ],

    sidebar: [
      {
        text: 'App',
        items: [
          { text: 'Download', link: '/download' },
          { text: 'Screenshots', link: '/screenshots' }
        ]
      },
      {
        text: 'Other',
        items: [
          { text: 'Contact', link: '/contact' },
          { text: 'Funding', link: '/funding' }
        ]
      },
      {
        text: 'Docs',
        items: [
          { text: "Translate", link: "/docs/translate" },
          { text: "Build & Config", link: "/docs/build-and-config" },
          { 
            text: 'Features', 
            items: [
              { text: "Share wrapping", link: "/docs/features/share-wrapping" },
              { text: "Embedding videos inside steps", link: "/docs/features/embedding-videos-inside-steps" },
              { text: "Templating", link: "/docs/features/templating" },
            ]
          }
        ]
      }
    ],

    footer: {
      message: "Icon made by Freepik from www.flaticon.com",
      copyright: "kitshn.app and kitshn are available under <a href='https://github.com/aimok04/kitshn/blob/main/LICENSE'>GNU General Public License v3.0</a>"
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/aimok04/kitshn' },
      { icon: 'kofi', link: 'https://ko-fi.com/aimok04' }
    ]
  },
  sitemap: {
    hostname: "https://kitshn.app"
  },
  head: [
    ["meta", { name: "darkreader-lock" }],
    ["link", { rel: "apple-touch-icon", sizes: "57x57", href: "/apple-icon-57x57.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "60x60", href: "/apple-icon-60x60.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "72x72", href: "/apple-icon-72x72.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "76x76", href: "/apple-icon-76x76.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "114x114", href: "/apple-icon-114x114.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "120x120", href: "/apple-icon-120x120.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "144x144", href: "/apple-icon-144x144.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "152x152", href: "/apple-icon-152x152.png" }],
    ["link", { rel: "apple-touch-icon", sizes: "180x180", href: "/apple-icon-180x180.png" }],
    ["link", { rel: "icon", sizes: "192x192", href: "/android-icon-192x192.png" }],
    ["link", { rel: "icon", sizes: "32x32", href: "/favicon-32x32.png" }],
    ["link", { rel: "icon", sizes: "96x96", href: "/favicon-96x96.png" }],
    ["link", { rel: "icon", sizes: "16x16", href: "/favicon-16x16.png" }],
    ["link", { rel: "manifest", href: "/manifest.json" }],
    ["meta", { name: "msapplication-TileColor", content: "#FED506" }],
    ["meta", { name: "msapplication-TileImage", content: "/ms-icon-144x144.png" }],
    ["meta", { name: "theme-color", content: "#FED506" }]
  ]
})