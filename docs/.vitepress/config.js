import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "Stuff+",
  description: "Advanced Modern Moderation & Utility Plugin for Paper and Folia",
  base: "/StuffPlus/", // Base path matching the repository name for GitHub Pages
  themeConfig: {
    logo: '/logo.png',
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' }
    ],
    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'Getting Started', link: '/guide/getting-started' }
        ]
      },
      {
        text: 'Feature Details',
        items: [
          { text: 'Commands & Permissions', link: '/guide/commands' },
          { text: 'Configuration Reference', link: '/guide/configuration' }
        ]
      },
      {
        text: 'Developer Best Practices',
        items: [
          { text: 'Folia Compatibility', link: '/guide/folia' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/synkfr/StuffPlus' }
    ],
    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2026-present synkfr & Antigravity'
    }
  }
})
