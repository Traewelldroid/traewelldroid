# Träwelldroid

This is a community app for the open source project
[Träwelling](https://github.com/Traewelling/traewelling).

[![Übersetzungsstatus](https://translate.codeberg.org/widgets/traewelldroid/-/svg-badge.svg)](https://translate.codeberg.org/engage/traewelldroid/)
[![Gitmoji](https://img.shields.io/badge/gitmoji-%20😜%20😍-FFDD67.svg)](https://gitmoji.dev)

## Find us on

- [traewelldroid.de](https://traewelldroid.de)
- <a href="https://zug.network/@traewelldroid" rel="nofollow me">Mastodon</a>
- [Twitter](https://twitter.com/@traewelldroid)

## Download the app

[<img src="https://raw.githubusercontent.com/Traewelldroid/traewelldroid/dev/assets/badges/google-play-badge.png" alt="Download on Google Play" width="240">](https://play.google.com/store/apps/details?id=de.hbch.traewelling)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Download on F-Droid" width="240">](https://f-droid.org/packages/de.hbch.traewelling)

## How it is developed

The app is using the official Jetpack Compose framework by Google. Crashes of the app are detected
and transmitted by [Sentry](https://sentry.io). For the production version of the app deployed to
Google Play, a self-hosted Sentry instance is used. Feature Flags are possible and implemented via
a self-hosted Unleash instance.

## Setup

Personalised properties can be configured in the app-level `build.gradle` file.

| Property name        | Usage                                       | Mandatory |
| -------------------- | ------------------------------------------- | --------- |
| `SENTRY_DSN`         | The DSN used for error logging with Sentry. | No        |
| `OAUTH_CLIENT_ID`    | The OAuth client id                         | Yes       |
| `OAUTH_REDIRECT_URL` | The OAuth redirect URL                      | Yes       |
| `REPO_URL`           | The URL to the repository                   | No        |
| `PRIVACY_URL`        | URL to the privacy statement and imprint.   | Yes       |
| `UNLEASH_URL`        | API URL to reach the Unleash API            | No        |
| `UNLEASH_KEY`        | API Key for Unleash API                     | No        |
| `WEBHOOK_URL`        | URL for accepting Träwelling webhooks       | No        |

## Contributions

I'm always happy for contributions in any way! Is there something that could be improved on
Träwelldroid? Just [create an issue](https://github.com/Traewelldroid/traewelldroid/issues/new/choose)
and tell us about your concerns or ideas!

Of course you're also free to contribute code by yourself! Just fork the repo, do your changes and
simply submit a PR. Thank you! 😊
