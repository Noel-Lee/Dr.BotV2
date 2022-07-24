from asyncio.base_futures import _FINISHED
from os import access

import logging
from this import d
import datetime
from datetime import timedelta
import pytz

from telegram import *
from telegram.ext import *

import pyrebase

config = {
  "apiKey": "apiKey",
  "authDomain": "projectId.firebaseapp.com",
  "databaseURL": "https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/",
  "storageBucket": "projectId.appspot.com"
}

firebase = pyrebase.initialize_app(config)
database = firebase.database()

### Enable logging
logging.basicConfig(
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.INFO
)

logger = logging.getLogger(__name__)

token = "5350141837:AAFsROe0cVeOw1TeSvMvsxLMWK1VVCYN93E"

EXPECT_GENDER, EXPECT_BIRTHYEAR = range(2)
EXPECT_MEDICATION_NAME, EXPECT_DURATION, EXPECT_FREQUENCY, EXPECT_TIMINGS = range(4)
EXPECT_CANCEL_REMINDER_NAME, EXPECT_CONFIRMATION = range(2)
EXPECT_EDIT_REMINDER_NAME, EXPECT_EDIT, EXPECT_DONE = range(3)
EXPECT_DONE_NAME = range(1)

gender_buttons = [[KeyboardButton("Male")], [KeyboardButton("Female")], [KeyboardButton("Prefer not to say")]]
confirmation_buttons = [[KeyboardButton("Yes")], [KeyboardButton("No")]]
done_button = [[KeyboardButton("Done")]]

TIME_DISPARITY = 3900
ONE_DAY = 86400
FIVE_MINUTES = 300
MINUTES_PER_HOUR = 60
TIMES_UNTIL_LATE = 12

### Command handlers
def start(update: Update, context: CallbackContext) -> None:
    """Send a message when the command /start is issued."""
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      output ='You have already set up an account. Please use /help to view other available commands.'
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      chat_id = str(update.message["chat"]["id"])
      update_reminders(update, context, chat_id)
      return ConversationHandler.END
    else:
      user = update.effective_user
      markup = ReplyKeyboardMarkup(keyboard = gender_buttons, one_time_keyboard = True)
      text ='Hi! In order to start using Dr.Bot, we are going to require some details first. Please select your gender.'
      update.message.reply_text(text, reply_markup = markup)
      return EXPECT_GENDER

def gender_input(update: Update, context: CallbackContext):
    if update.message.text in ["Male", "Female", "Prefer not to say"]:
      output = "Please enter your birth year in YYYY format."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      context.user_data['gender'] = update.message.text
      return EXPECT_BIRTHYEAR
    else:
      markup = ReplyKeyboardMarkup(keyboard = gender_buttons, one_time_keyboard = True)
      update.message.reply_text("Please select a button.", reply_markup = markup)
      return EXPECT_GENDER

def birthyear_input(update: Update, context: CallbackContext):
    if len(update.message.text) == 4 and update.message.text.isnumeric():
      output = "Thank you for entering your details. Your account has been set-up."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      teleHandle = update.message.from_user.username
      user_gender = context.user_data.get('gender')
      # list1 = connection.execute("SELECT * FROM Member WHERE teleHandle = '{}';".format(teleHandle)).fetchall()
      # if list1:
      #   connection.execute("UPDATE Member \
      #               SET password = 'default', gender = '{}', birthYear = '{}', EXP = '{}', levels = '{}' \
      #                   WHERE teleHandle = '{}'".format(user_gender, update.message.text, 0, 0, teleHandle))
      # else:
      #   tuple1 = (teleHandle, "default", user_gender, update.message.text, 0, 0)
      #   connection.execute("INSERT INTO Member VALUES {}".format(tuple1))
      data = {"birthYear": update.message.text, "email": "","exp": 0, "fullName": "", "gender": user_gender, "password": "default", "password2": "default", "phoneNo": "", "teleHandle": teleHandle, "timelyConsumption": 0, "totalConsumption": 0}
      database.child("Users").child(teleHandle).set(data)
      return ConversationHandler.END
    else:
      update.message.reply_text("Please enter your birth year in the valid format.")
      return EXPECT_BIRTHYEAR

def cancel(update, context: CallbackContext):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    update.message.reply_text("Setting up of account cancelled.")
    return ConversationHandler.END

def help_command(update: Update, context: CallbackContext) -> None:
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    update.message.reply_text('These are the list of commands you can use.\n/start - Create a new account. \n/help - Bring up a list of valid commands. \n/set_reminder - Start setting a new reminder. \n/edit_reminder - Edit reminders for a medication that you have already set. \n/cancel_reminder - Cancel reminders for a medication that you have already set. \n/done - Record your medication consumption. \n/summary - View a list of your current medication.')

def messageHandler(update: Update, context: CallbackContext) -> None:
    user = update.effective_user
    output = 'Use /help for a list of valid commands.'
    context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    
### REMINDER FUNCTION ###

def start_reminder(update, context):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      update.message.reply_text("What is the name of the medication?", parse_mode="markdown")
      return EXPECT_MEDICATION_NAME
    else:
      update.message.reply_text("You have not registered yet. Please create an account using /start.", parse_mode="markdown")
      return ConversationHandler.END

def medication_name(update, context):
    if not update.message.text.isnumeric():
      output = "What is the duration of the presciption in days?"
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      context.user_data['medication_name'] = update.message.text
      return EXPECT_DURATION
    else:
      update.message.reply_text("Medication name should not consist of numbers. Please enter a valid medication name.")
      return EXPECT_MEDICATION_NAME

def medication_duration(update, context):
    if update.message.text.isnumeric() and int(update.message.text) >= 1:
      output = "What is the daily frequency of the presciption?"
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      context.user_data['medication_duration'] = int(update.message.text)
      return EXPECT_FREQUENCY
    elif update.message.text.isnumeric() and int(update.message.text) <= 0:
      update.message.reply_text("Please enter a duration of more than 0. For example, '2' for a duration of 2 days.")
      return EXPECT_DURATION
    else:
      update.message.reply_text("Please enter a valid duration. For example, '2' for a duration of 2 days.")
      return EXPECT_DURATION

def medication_frequency(update, context):
    if update.message.text.isnumeric() and int(update.message.text) >= 1:
      output = "What are the timings at which you would like to receive a reminder? Please enter your answers in HH:MM format, with a space between for frequencies of more than 1. \n \nFor example, '10:00 18:00' for a medication to be taken twice daily at 10am and at 6pm."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      context.user_data['medication_frequency'] = int(update.message.text)
      return EXPECT_TIMINGS
    elif update.message.text.isnumeric() and int(update.message.text) <= 0:
      update.message.reply_text("Please enter a frequency of more than 0. For example, '2' for a frequency of twice a day.")
      return EXPECT_FREQUENCY
    else:
      update.message.reply_text("Please enter a valid frequency. For example, '2' for a frequency of twice a day.")
      return EXPECT_FREQUENCY

def medication_timings(update, context):
    chat_id = str(update.message["chat"]["id"])
    frequency = context.user_data['medication_frequency']
    reminder_list = []
    if (frequency == 1 and len(update.message.text) == 5):
      context.user_data['medication_timings'] = update.message.text
      reminder_list.append(update.message.text)
    elif (frequency != 1 and len(update.message.text) == 5 * frequency + (frequency - 1)):
      context.user_data['medication_timings'] = update.message.text
      for i in range(frequency):
        reminder_list.append(context.user_data['medication_timings'].split(" ")[i])
    else:
      update.message.reply_text("Please enter the timings in a valid format. For example, '10:00 18:00' for a medication to be taken twice daily at 10am and at 6pm.")
      return EXPECT_TIMINGS
    for timing in reminder_list:
      if int(timing.split(":")[0]) > 23:
        update.message.reply_text(f"'{timing}' is not a valid time. For hours after midnight, please use the 24 hour clock format. For example, '00:30' for a medication to be taken at 12:30am.")
        return EXPECT_TIMINGS
      if int(timing.split(":")[0]) < 0:
        update.message.reply_text(f"'{timing}' is not a valid time. For hours after midnight, please use the 24 hour clock format. For example, '00:30' for a medication to be taken at 12:30am.")
        return EXPECT_TIMINGS
      if int(timing.split(":")[1]) > 59:
        update.message.reply_text(f"'{timing}' is not a valid time as minutes can not be more than 59. Please enter the timings in a valid format. For example, '10:00 18:00' for a medication to be taken twice daily at 10am and at 6pm.")
        return EXPECT_TIMINGS
      if int(timing.split(":")[1]) < 0:
        update.message.reply_text(f"'{timing}' is not a valid time. Please enter the timings in a valid format. For example, '10:00 18:00' for a medication to be taken twice daily at 10am and at 6pm.")
        return EXPECT_TIMINGS
    current_time = datetime.datetime.now(tz=pytz.timezone('Asia/Singapore'))
    current_year = datetime.date.today().year
    current_month = datetime.date.today().month
    current_day = datetime.date.today().day
    teleHandle = update.message.from_user.username
    med_name = context.user_data.get('medication_name')
    job_id = f'{teleHandle}-med_name'
    days = context.user_data.get('medication_duration')
    timings = context.user_data.get('medication_timings')
    last_timing = len(reminder_list)
    for i in range(days):
      for j in range(last_timing):
        h, m = int(reminder_list[j].split(":")[0]), int(reminder_list[j].split(":")[1])
        time = datetime.datetime(current_time.year, current_time.month, current_time.day, hour=h, minute=m, tzinfo=pytz.timezone('Asia/Singapore'))
        time_diff = time - current_time
        seconds_diff = time_diff.total_seconds() - TIME_DISPARITY 
        if seconds_diff < 0:
          seconds_diff += ONE_DAY 
        seconds_diff += i * ONE_DAY
        new_time = current_time + datetime.timedelta(seconds = seconds_diff)
        context.job_queue.run_once(notification, new_time, context=[chat_id, med_name, days, frequency, timings, False, reminder_list[j], new_time, teleHandle, True], name = teleHandle)
    timings_comma = timings.replace(" ", ",")
    endDate = f"{new_time.year}-{new_time.month}-{new_time.day}"
    data = {"consumable_name": med_name, "duration": f"{days}", "frequency": f"{frequency}", "reminder_times": timings_comma, "remarks": "", "untilDate": endDate}
    database.child("Users").child(teleHandle).child("Consumables").child(med_name).set(data)
    context.bot.send_message(chat_id = update.effective_chat.id, text="Reminder has been set successfully.")
    return ConversationHandler.END

def notification(context):
    job = context.job
    med_name = job.context[1]
    teleHandle = job.context[8]
    context.bot.send_message(job.context[0], text=f'Reminder to take {med_name}! \n\nPlease acknowledge this by using the /done command when you have taken {med_name}.')
    data = {"expected_time": job.context[6], "taken": False, "late_count": 0}
    database.child("Users").child(teleHandle).child("Untaken").child(med_name).set(data)
    new_time = job.context[7] 
    new_time += datetime.timedelta(seconds = FIVE_MINUTES)
    new_context = [job.context[0], job.context[1], job.context[2], job.context[3], job.context[4], job.context[5], job.context[6], new_time, job.context[8], False]
    context.job_queue.run_once(repeat_notification, new_time, context = new_context, name = teleHandle)

def repeat_notification(context):
    job = context.job
    med_name = job.context[1]
    teleHandle = job.context[8]
    new_time = job.context[7]
    taken = database.child("Users").child(teleHandle).child("Untaken").child(med_name).child("taken").get()
    if taken.val() == False:
      context.bot.send_message(job.context[0], text=f'Reminder to take {med_name}! \n\nPlease acknowledge this by using the /done command when you have taken {med_name}.')
      new_time += datetime.timedelta(seconds = FIVE_MINUTES)
      new_context = [job.context[0], job.context[1], job.context[2], job.context[3], job.context[4], job.context[5], job.context[6], new_time, job.context[8], False]
      late_count = database.child("Users").child(teleHandle).child("Untaken").child(med_name).child("late_count").get()
      new_count = late_count.val() + 1
      database.child("Users").child(teleHandle).child("Untaken").child(med_name).update({"late_count": new_count})
      context.job_queue.run_once(repeat_notification, new_time, context = new_context, name = teleHandle)
    else:
      database.child("Users").child(teleHandle).child("Untaken").child(med_name).remove()

### DONE TAKING FUNCTION ###

def done(update, context):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      teleHandle = update.message.from_user.username
      meds = database.child("Users").child(teleHandle).child("Untaken").get()
      if meds.val():
        list_of_meds = meds.val()
        med_buttons = []
        for med in list_of_meds:
          button_name = f"{med}"
          med_buttons.append([KeyboardButton(button_name)])
        output = "Which medication have you taken?"
        markup = ReplyKeyboardMarkup(keyboard = med_buttons, one_time_keyboard = True)
        update.message.reply_text(output, reply_markup = markup)
        context.user_data['untaken_meds'] = list_of_meds
        return EXPECT_DONE_NAME
      else:
        output = "You do not currently have any medication to be taken."
        context.bot.send_message(
            chat_id = update.effective_chat.id,
            text = output
        )
        return ConversationHandler.END
    else:
      update.message.reply_text("You have not registered yet. Please create an account using /start.", parse_mode="markdown")
      return ConversationHandler.END

def done_name(update, context):
    list_of_meds = context.user_data.get('untaken_meds')
    teleHandle = update.message.from_user.username
    med_name = update.message.text
    if med_name in list_of_meds:
      taken = database.child("Users").child(teleHandle).child("Untaken").child(med_name).child("taken").get()
      if taken.val() == False:
        output = f"{med_name} has been recorded as taken."
        data = {"taken": True}
        database.child("Users").child(teleHandle).child("Untaken").child(med_name).update(data)
        late_count = database.child("Users").child(teleHandle).child("Untaken").child(med_name).child("late_count").get()
        times_late = late_count.val()
        if times_late < TIMES_UNTIL_LATE:
          current_timely_consumption = database.child("Users").child(teleHandle).child("timelyConsumption").get()
          if current_timely_consumption.val():
            new_timely_consumption = current_timely_consumption.val() + 1
          else:
            new_timely_consumption = 1
          current_exp = database.child("Users").child(teleHandle).child("exp").get()
          if current_exp.val():
            new_exp = current_exp.val() + 10
          else:
            new_exp = 10
          data = {"exp": new_exp, "timelyConsumption": new_timely_consumption}
          database.child("Users").child(teleHandle).update(data)
        current_total_consumption = database.child("Users").child(teleHandle).child("totalConsumption").get()
        if current_total_consumption.val():
          new_total_consumption = current_total_consumption.val() + 1
        else:
          new_total_consumption = 1
        database.child("Users").child(teleHandle).update({"totalConsumption": new_total_consumption})
        context.bot.send_message(
            chat_id = update.effective_chat.id,
            text = output
        )
        last = True
        for job in context.job_queue.get_jobs_by_name(teleHandle):
          if job.context[1] == med_name and job.context[9] == True:
            last = False
        if last == True:
          database.child("Users").child(teleHandle).child("Consumables").child(med_name).remove()
          context.bot.send_message(job.context[0], text=f'You have completed your presciption for {med_name}.')
        return ConversationHandler.END
      else:
        update.message.reply_text(f'{med_name} has already been recorded as taken.')
        return ConversationHandler.END
    else:
      output = f"{med_name} is not a medication that you are required to take now. Please select a medication from the buttons provided."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      return EXPECT_DONE_NAME

### CANCEL REMINDER FUNCTION ###

def cancel_reminder(update, context):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      output = "What is the name of the medication that you would like to cancel reminders for?"
      list = med_list(update, context)
      med_buttons = []
      if list == []:
        update.message.reply_text("You currently have no reminders to cancel.")
        return ConversationHandler.END
      else:
        for med in list:
          med_buttons.append([KeyboardButton(med)])
        markup = ReplyKeyboardMarkup(keyboard = med_buttons, one_time_keyboard = True)
        update.message.reply_text(output, reply_markup = markup)
        return EXPECT_CANCEL_REMINDER_NAME
    else:
      update.message.reply_text("You have not registered yet. Please create an account using /start.", parse_mode="markdown")
      return ConversationHandler.END

def reminder_name(update, context):
    if update.message.text in med_list(update, context):
      med_name = update.message.text
      output = f"Are you sure you want to cancel reminders for {med_name}?"
      markup = ReplyKeyboardMarkup(keyboard = confirmation_buttons, one_time_keyboard = True)
      update.message.reply_text(output, reply_markup = markup)
      context.user_data['cancel_med'] = update.message.text
      return EXPECT_CONFIRMATION
    else:
      update.message.reply_text("Please select a valid medication name which you have set a reminder for.")
      return EXPECT_CANCEL_REMINDER_NAME

def confirmation(update, context):
    med_name = context.user_data.get('cancel_med')
    teleHandle = update.message.from_user.username
    if update.message.text == "Yes":
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        if job.context[1] == med_name:
          job.schedule_removal()
      output = f"Reminders for {med_name} successfully cancelled."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      database.child("Users").child(teleHandle).child("Consumables").child(med_name).remove()
      return ConversationHandler.END
    elif update.message.text == "No":
      update.message.reply_text(f"Reminders for {med_name} will not be cancelled.")
      return ConversationHandler.END
    else:
      markup = ReplyKeyboardMarkup(keyboard = confirmation_buttons, one_time_keyboard = True)
      update.message.reply_text("Please select a button.", reply_markup = markup)
      return EXPECT_CONFIRMATION

### EDIT REMINDER FUNCTION ###

def edit_reminder(update, context):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      output = "What is the name of the medication that you would like to edit reminders for?"
      list = med_list(update, context)
      med_buttons = []
      if list == []:
        update.message.reply_text("You currently have no reminders to edit.")
        return ConversationHandler.END
      else:
        for med in list:
          med_buttons.append([KeyboardButton(med)])
        markup = ReplyKeyboardMarkup(keyboard = med_buttons, one_time_keyboard = True)
        update.message.reply_text(output, reply_markup = markup)
        return EXPECT_EDIT_REMINDER_NAME
    else:
      update.message.reply_text("You have not registered yet. Please create an account using /start.", parse_mode="markdown")
      return ConversationHandler.END

def edit_reminder_name(update, context):
    if update.message.text in med_list(update, context):
      med_name = update.message.text
      teleHandle = update.message.from_user.username
      reminders = []
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        if job.context[1] == med_name:
          reminders = job.context[4]
          frequency = job.context[3]
          break
      reminder_timings = []
      if frequency == 1:
        reminder_timings.append(reminders)
      else:
        for i in range(frequency):
          reminder_timings.append(reminders.split(" ")[i])
      timing_buttons = []
      for timing in reminder_timings:
        button_name = f"{timing}"
        timing_buttons.append([KeyboardButton(button_name)])
      output = f"Which reminder timing for {med_name} would you like to edit?"
      markup = ReplyKeyboardMarkup(keyboard = timing_buttons, one_time_keyboard = True)
      update.message.reply_text(output, reply_markup = markup)
      context.user_data['edit_med'] = med_name
      context.user_data['edit_frequency'] = frequency
      context.user_data['edit_all_timings'] = reminder_timings
      return EXPECT_EDIT
    else:
      update.message.reply_text("Please select a valid medication name which you have set a reminder for.")
      return EXPECT_EDIT_REMINDER_NAME

def edit(update, context):
    if update.message.text not in context.user_data.get('edit_all_timings'):
      update.message.reply_text("Please select a valid medication reminder timing which you would like to edit.")
      return EXPECT_EDIT
    else:
      context.user_data['edit_timing'] = update.message.text
      output = "What new timing would you like to change the reminder to? Please enter your timing in HH:MM format."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      return EXPECT_DONE

def done_editing(update, context):
    if len(update.message.text) != 5:
      update.message.reply_text("The timing you have provided is of invalid format. Please enter your timing in HH:MM format.")
      return EXPECT_DONE
    else:
      chat_id = str(update.message["chat"]["id"])
      teleHandle = update.message.from_user.username
      med_name = context.user_data.get('edit_med')
      frequency = context.user_data.get('edit_frequency')
      old_timing = context.user_data.get('edit_timing')
      new_timing = update.message.text
      i = 0
      done = False
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        if job.context[1] == med_name:
          if job.context[6] == old_timing and done == False:
            h, m = int(new_timing.split(":")[0]), int(new_timing.split(":")[1])
          else:
            h, m = int(job.context[6].split(":")[0]), int(job.context[6].split(":")[1])
          current_time = datetime.datetime.now(tz=pytz.timezone('Asia/Singapore'))
          time = datetime.datetime(current_time.year, current_time.month, current_time.day, hour=h, minute=m, tzinfo=pytz.timezone('Asia/Singapore'))
          time_diff = time - current_time
          seconds_diff = time_diff.total_seconds() - TIME_DISPARITY 
          if seconds_diff < 0:
            seconds_diff += ONE_DAY 
          seconds_diff += (i // frequency) * ONE_DAY
          new_time = current_time + datetime.timedelta(seconds = seconds_diff)
          old_timings = job.context[4]
          new_timings = old_timings.replace(old_timing, new_timing, 1)
          days = job.context[2]
          if job.context[6] == old_timing and done == False:
            context.job_queue.run_once(notification, seconds_diff, context=[chat_id, med_name, job.context[2], job.context[3], new_timings, job.context[5], new_timing, new_time, teleHandle, True], name = teleHandle)
            done = True
          else:
            context.job_queue.run_once(notification, seconds_diff, context=[chat_id, med_name, job.context[2], job.context[3], new_timings, job.context[5], job.context[6], job.context[7], teleHandle, job.context[9]], name = teleHandle)
          job.schedule_removal()
          i += 1
      # database.child("Users").child(teleHandle).child("Consumables").child(med_name).remove()
      timings_comma = new_timings.replace(" ", ",")
      endDate = f"{new_time.year}-{new_time.month}-{new_time.day}"
      data = {"consumable_name": med_name, "duration": f"{days}", "frequency": f"{frequency}", "reminder_times": timings_comma, "remarks": "", "untilDate": endDate}
      database.child("Users").child(teleHandle).child("Consumables").child(med_name).update(data)
      output = f"Reminders for {med_name} successfully edited."
      context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )
      return ConversationHandler.END

### UPDATE ACCORDING TO DATABASE ###

def update_reminders(update, context, chat_id):
    teleHandle = update.message.from_user.username
    meds = database.child("Users").child(teleHandle).child("Consumables").get()
    if meds.val():
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        if job.context[1] not in meds.val():
          job.schedule.removal()
      for med in meds.val():
        skip = False
        if med in med_list(update, context):
          timings_comma = database.child("Users").child(teleHandle).child("Consumables").child(med).child("reminder_times").get().val()
          timings = timings_comma.replace(",", " ")
          for job in context.job_queue.get_jobs_by_name(teleHandle):
            if job.context[1] == med and job.context[4] != timings:
              ### maybe need to replace jobs one by one not all at once
              job.schedule_removal()
            elif job.context[1] == med and job.context[4] == timings:
              skip = True
        if skip == False:
          current_time = datetime.datetime.now(tz=pytz.timezone('Asia/Singapore'))
          med_name = database.child("Users").child(teleHandle).child("Consumables").child(med).child("consumable_name").get().val()
          days = database.child("Users").child(teleHandle).child("Consumables").child(med).child("duration").get().val()
          reminder_times = database.child("Users").child(teleHandle).child("Consumables").child(med).child("reminder_times").get().val()
          reminder_times_no_comma = reminder_times.replace(",", " ")
          timings = []
          frequency = len(reminder_times) // 5
          if frequency == 1:
            timings.append(reminder_times)
          else:
            for i in range(frequency):
              timings.append(reminder_times.split(",")[i])
          for j in range(int(days)):          
            for timing in timings:
              h, m = int(timing.split(":")[0]), int(timing.split(":")[1])
              time = datetime.datetime(current_time.year, current_time.month, current_time.day, hour=h, minute=m, tzinfo=pytz.timezone('Asia/Singapore'))
              time_diff = time - current_time
              seconds_diff = time_diff.total_seconds() - TIME_DISPARITY 
              if seconds_diff < 0:
                seconds_diff += ONE_DAY 
              seconds_diff += j * ONE_DAY
              new_time = current_time + datetime.timedelta(seconds = seconds_diff)           
              context.job_queue.run_once(notification, new_time, context=[chat_id, med_name, days, frequency, reminder_times_no_comma, False, timing, new_time, teleHandle, True], name = teleHandle)
    else:
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        job.schedule_removal()

### GET MEDICATION LIST ###

def med_list(update, context):
    meds = []
    teleHandle = update.message.from_user.username
    for job in context.job_queue.get_jobs_by_name(teleHandle):
      if job.context[1] not in meds:
        meds.append(job.context[1])
    return meds


### SUMMARY OF MEDICATION ###

def summary(update, context):
    chat_id = str(update.message["chat"]["id"])
    update_reminders(update, context, chat_id)
    teleHandle = update.message.from_user.username
    users = database.child("Users").get()
    if teleHandle in users.val():
      teleHandle = update.message.from_user.username
      output = "The following are the medications you are currently prescribed.\n"
      for job in context.job_queue.get_jobs_by_name(teleHandle):
        if job.context[1] not in output:
          output += f"\n{job.context[1]}: {job.context[3]} time(s) a day for {job.context[2]} day(s) at the following timings - {job.context[4]}"
      if output == "The following are the medications you are currently prescribed.\n":
        output = "You are not currently on any medication."
      context.bot.send_message(
            chat_id = update.effective_chat.id,
            text = output
        )
    else:
      update.message.reply_text("You have not registered yet. Please create an account using /start.", parse_mode="markdown")
      return ConversationHandler.END

def testing(update, context):
    teleHandle = update.message.from_user.username
    output = "testing\n"
    for job in context.job_queue.get_jobs_by_name(teleHandle):
      output += f"\n{job.context[1]}: {job.context[7]}"
    if output == "testing\n":
      output = "You are not currently on any medication."
    context.bot.send_message(
          chat_id = update.effective_chat.id,
          text = output
      )

def main() -> None:
    updater = Updater(token)
    bot = updater.bot
    job_queue = updater.job_queue

    dispatcher = updater.dispatcher

    ### various commands that the bot can handle
    dispatcher.add_handler(CommandHandler("help", help_command))

    dispatcher.add_handler(CommandHandler("summary", summary))

    dispatcher.add_handler(CommandHandler("testing", testing))

    dispatcher.add_handler(ConversationHandler(entry_points = [CommandHandler("start", start)],
                                               states = {EXPECT_GENDER: [MessageHandler(Filters.text, gender_input)],
                                                         EXPECT_BIRTHYEAR : [MessageHandler(Filters.text, birthyear_input)]
                                                         },
                                               fallbacks = [CommandHandler('cancel', cancel)]
                                               )
    )

    dispatcher.add_handler(ConversationHandler(entry_points = [CommandHandler("set_reminder", start_reminder)],
                                               states = {EXPECT_MEDICATION_NAME: [MessageHandler(Filters.text, medication_name)],
                                                         EXPECT_DURATION : [MessageHandler(Filters.text, medication_duration)],
                                                         EXPECT_FREQUENCY : [MessageHandler(Filters.text, medication_frequency)],
                                                         EXPECT_TIMINGS : [MessageHandler(Filters.text, medication_timings)]
                                                         },
                                               fallbacks = [CommandHandler('cancel', cancel)]
                                               )
    )

    dispatcher.add_handler(ConversationHandler(entry_points = [CommandHandler("cancel_reminder", cancel_reminder)],
                                               states = {EXPECT_CANCEL_REMINDER_NAME: [MessageHandler(Filters.text, reminder_name)],
                                                         EXPECT_CONFIRMATION : [MessageHandler(Filters.text, confirmation)]
                                                         },
                                               fallbacks = [CommandHandler('cancel', cancel)]
                                               )
    )

    dispatcher.add_handler(ConversationHandler(entry_points = [CommandHandler("edit_reminder", edit_reminder)],
                                               states = {EXPECT_EDIT_REMINDER_NAME: [MessageHandler(Filters.text, edit_reminder_name)],
                                                         EXPECT_EDIT : [MessageHandler(Filters.text, edit)],
                                                         EXPECT_DONE : [MessageHandler(Filters.text, done_editing)]
                                                         },
                                               fallbacks = [CommandHandler('cancel', cancel)]
                                               )
    )

    dispatcher.add_handler(ConversationHandler(entry_points = [CommandHandler("done", done)],
                                               states = {EXPECT_DONE_NAME: [MessageHandler(Filters.text, done_name)]                                                         
                                                         },
                                               fallbacks = [CommandHandler('cancel', cancel)]
                                               )
    )

    ### when user input is not a command
    dispatcher.add_handler(MessageHandler(Filters.text, messageHandler))

    updater.start_polling()

    updater.idle()


if __name__ == '__main__':
    main()
