# slack-team-happiness
Team happiness Slack Bot which populates the related Engineering Scorecard metric.

## Features
* Weekly Happiness Check-In: Automatically prompts members of the group `SLACK_TEAM_CHANNEL` every Friday to rate their happiness on a scale.

<img width="470" alt="Screenshot 2024-02-21 at 15 04 22" src="https://github.com/lindar-joy/slack-team-happiness/assets/18517177/2d64d3a8-8e91-46e7-af75-0a7a56f3c217">

* Pushed to Sheets: The results are automatically pushed to the Google Sheet specified in `TeamHappinessGoogleSheetService#SPREADSHEET_ID`.

## Contributing
### Building the project 
- Java 17 is required (i.e. `sdk use java 17.0.2-open`)
- Run `./gradlew build`

### Running locally
- Run `SlackTeamHappinessApplication` using your IDE with the variables (populate with relevant test values):

```
-DSLACK_APP_TOKEN= // test bot details
-DSLACK_BOT_TOKEN= // test bot details
-DSLACK_SIGNING_SECRET= // test bot details
-DSLACK_TEST_USER_ID= // your or another test user id
```

Get your Slack user ID:
1. Click on your profile picture
2. Click "View full profile"
3. Click on "more" and click "Copy member ID"

Additionally, you also need [Google Service Account Credentials](https://developers.google.com/workspace/guides/create-credentials#service-account) to be able to access Google Sheets, which need to reside in `<user.home>/conf/credentials.json`.

### Deploying to production
- Run `./gradlew bootJar` and deploy to Enscale using the Deployment Manager.
- Remember to use the variable `SLACK_TEAM_GROUP_ID` instead of `SLACK_TEST_USER_ID` when you are ready to send this message to more than one person.

