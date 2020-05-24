const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// function to notify anonymous users when a champion comes online
exports.sendChampionOnlineNotification = functions.database.ref('/Users/{pushId}')
    .onUpdate((change, context) => {
      // check if status was offline previously
      const beforeUser = change.before.val();
      const beforeStatus = beforeUser.status;
      if (beforeStatus === "offline") {
        // check if status is online now
        const afterUser = change.after.val();
        const afterStatus = afterUser.status;
        if (afterStatus === "online") {
          //only send notification if the status hasn't changed recently
          const beforeTime = Date.parse(beforeUser.status_change_time);
          const afterTime = Date.parse(afterUser.status_change_time);
          const thresholdTime = beforeTime + 3600000; // 1 hour
          // console.log('beforeTime ' + beforeTime);
          // console.log('afterTime' + afterTime);
          // console.log('thresholdTime' + thresholdTime);

          if (afterTime > thresholdTime) {
            // build notification
            const payload = {
              notification: {
                title: 'A Mental Health Champion is online',
                body: `${afterUser.username}`
              }
            };

            // send notifications to topic subscribers
            return admin.messaging()
              .sendToTopic("anonymousUsers",payload)
              .then(response => {
                return console.log('Notification sent successfully', response);
              })
              .catch(error => {
                console.log('Notification sent failed:',error);
              });
          }
        }
      }

    // no notification sent
    console.log('Change does not meet requirements, notification not sent');
    return null;
});

// need to get token from database

// send to individual device
// https://firebase.google.com/docs/cloud-messaging/send-message#send_to_individual_devices
