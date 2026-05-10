const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyNewCourse = onDocumentCreated("courses/{courseId}", async (event) => {
    const courseData = event.data.data();
    if (!courseData) return;

    const trade = courseData.trade;
    const title = courseData.title;

    console.log(`New course added: ${title} for trade ${trade}`);

    // Get all users
    const usersSnapshot = await admin.firestore().collection("users").get();
    
    const tokens = [];
    usersSnapshot.forEach((doc) => {
        const user = doc.data();
        // If the user's favoriteTrades contains this course's trade and they have a device token
        if (user.favoriteTrades && user.favoriteTrades.includes(trade) && user.fcmToken) {
            tokens.push(user.fcmToken);
        }
    });

    if (tokens.length === 0) {
        console.log("No users found interested in this trade with FCM tokens.");
        return null;
    }

    const payload = {
        notification: {
            title: "New Course Available! 🎉",
            body: `A new batch for ${title} is starting soon. Tap to check it out!`,
        },
        data: {
            courseId: event.params.courseId
        }
    };

    try {
        const response = await admin.messaging().sendToDevice(tokens, payload);
        console.log(`Successfully sent ${response.successCount} messages.`);
    } catch (error) {
        console.error("Error sending notification:", error);
    }

    return null;
});
