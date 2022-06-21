# Time-manager-studying

This is a time management app created for the coursework of the mobile development coursework.

The goal was to develop an app that keeps track of different modules. It allows the user to keep track of much they study and how far they are from mighting their target. Additionally, it allows for an online account to be used or backup. Although, the functionality of adding new accounts was not implemented.

The app contains an online guide that shows how to use the app, it can be found [here](app/src/main/assets). To access the HTML file please download the folder and open the HTML file.

The APK file of the app can be accessed [here](app-debug.apk).

## Overview

The main goal of my app is to track and register the time a student spends studying a certain subject/ module they have. This is as many students fail to keep track of how much effort they have put into a subject/module and as such, they may feel dissatisfied with their performance. The app will require the user to register how much time they have believed they should spend in total or how much time is expected from them. Furthermore, it will require the users to input the module weight, i.e. its importance. This helps with the prioritization of certain modules over others. Finally, the starting and ending date will be required. This is to allow tracking of how much effort has been put into studying for that module.

## Requirements

The app will require the user to register how much time they have believed they should spend in total or how much time is expected by them the module weight, i.e. its importance, and the starting and ending date. This will help with the prioritization of certain modules over others and tracking of how much effort has been put into studying for that module.

The recording functionality will allow for the start and end of the recording. It will record the time spent in the session as well as the date and time when the session recording ended. Users can also delete some sessions in case they recorded the wrong subject or had the recording functionality on by mistake.

Users can add and remove subjects, as well as alter the ones they have added. Furthermore, notifications will be sent to the user about not having met a certain percentage of effort made by a certain date. Also, notifications will be sent if the user has not been active for a while.

Another feature will be the addition of a starting and a ending date of subjects to the phone's calendar.

Finally, the dashboard page of the application will allow for an overview of the student’s performance. It will present the subject name, suggested effort and hours that have been dedicated.

## Environment needed to run the app

The app can run for SDK versions 23 to 30.

it utilizes Firebase, CardView and RecyclerView.
