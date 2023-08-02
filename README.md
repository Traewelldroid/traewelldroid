# Träwelldroid

This is a community app for the open source project
[Träwelling](https://github.com/Traewelling/traewelling).

[![Übersetzungsstatus](https://translate.codeberg.org/widgets/traewelldroid/-/svg-badge.svg)](https://translate.codeberg.org/engage/traewelldroid/)
[![Gitmoji](https://img.shields.io/badge/gitmoji-%20😜%20😍-FFDD67.svg)](https://gitmoji.dev)

## How it is developed

The app is using the official AndroidX framework by Google. Crashes of the app are detected and
transmitted by [Sentry](https://sentry.io). For the production version of the app a self-hosted
Sentry instance is used.

At the moment the app is available on Google Play. You can download the app there:
[Google Play Store](https://play.google.com/store/apps/details?id=de.hbch.traewelling)

## Setup

First of all you should copy the `key.properties.example` file in the root directory and copy the
contents into a `key.properties` (for release) or a `dev.properties` (for debug) file. With those 
properties the main keys and URLs of the application can be set.

| Property name        | Usage                                       | Mandatory |
|----------------------|---------------------------------------------|-----------|
| `SENTRY_DSN`         | The DSN used for error logging with Sentry. | No        |
| `OAUTH_CLIENT_ID`    | The OAuth client id                         | Yes       |
| `OAUTH_REDIRECT_URL` | The OAuth redirect URL                      | Yes       |
| `REPO_URL`           | The URL to the repository                   | No        |
| `PRIVACY_URL`        | URL to the privacy statement and imprint.   | Yes       |
| `UNLEASH_URL`        | API URL to reach the Unleash API            | No        |
| `UNLEASH_KEY`        | API Key for Unleash API                     | No        |

After that, open Android Studio and the project should setup itself automatically.

### Unleash

Träwelldroid uses feature toggles with [Unleash](https://getunleash.io) so that features already can
be implemented although they aren't rolled out on depending APIs yet. The official Träwelldroid
build uses a self-hosted Unleash variant hosted in Germany.

## Contributions

I'm always happy for contributions in any way! Is there something that could be improved on
Träwelldroid? Just [create an issue](https://github.com/Traewelldroid/traewelldroid/issues/new/choose)
and tell us about your concerns or ideas!

Of course you're also free to contribute code by yourself! Just fork the repo, do your changes and
simply submit a PR. Thank you! 😊
