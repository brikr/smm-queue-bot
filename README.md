# smm-queue-bot
Twitch IRC Bot for managing queues of user submitted levels
## Commands:
Viewers: use `!submit` in a chat submit a level. Example usage:
```
!submit BC5E-0000-00D4-CD7C
```
You can also add the word "present" to the end of your submission. If you do, the level will only be played if you are currently in chat.
```
!submit BC5E-0000-00D4-CD7C present
```
Streamers: use `!next` to pull the next level from the queue. This will remove the highest priority level from your queue that satisfies any `present` tags, and print information about it in chat.