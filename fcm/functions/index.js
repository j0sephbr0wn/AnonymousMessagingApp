const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// function to notify anonymous users when a champion comes online
exports.sendChampionOnlineNotification = functions.database.ref('/Users/{pushId}')
    .onUpdate((change, context) => {
      console.log('Change to Users detected');
      // get before and after data
      const beforeUser = change.before.val();
      const afterUser = change.after.val();

      // proceed if update is to a champion user
      if (beforeUser.champion === true) {
        console.log('Change is to a Champion');
        // get before and after online times
        const beforeTime = Date.parse(beforeUser.status_online_time);
        const afterTime = Date.parse(afterUser.status_online_time);

        // define threshold time
        const thresholdTime = beforeTime + 3600000; // 1 hour

        // console.log('beforeTime: ' + beforeTime);
        // console.log('afterTime: ' + afterTime);
        // console.log('thresholdTime: ' + thresholdTime);

        // send notification if time between being online is greater than threshold
        if (afterTime > thresholdTime) {
          console.log('Online threshold met');
          // build notification
          const payload = {
            notification: {
              title: 'Fancy a chat?',
              body: `${afterUser.username}` + " is currently online"
            }
          };

          // send notifications to topic subscribers
          return admin.messaging()
            .sendToTopic("anonymousUsers", payload)
            .then(response => {
              return console.log('Notification sent successfully', response);
            })
            .catch(error => {
              console.log('Notification send failed:', error);
            });
        }
      }

    // no notification sent
    console.log('Change does not meet requirements, no notification sent');
    return null;
});

// need to get token from database

// send to individual device
// https://firebase.google.com/docs/cloud-messaging/send-message#send_to_individual_devices
