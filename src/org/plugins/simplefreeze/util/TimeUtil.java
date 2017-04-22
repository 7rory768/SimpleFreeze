package org.plugins.simplefreeze.util;

/**
 * Created by Rory on 11/21/2016.
 */
public class TimeUtil {

    public static boolean isInt(String arg) {
        try {
            Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static int getUnitCount(String arg) {
        int count = 0;
        if (arg.length() > 1) {
            if (TimeUtil.isInt(arg.substring(0, arg.length() - 1))) {
                if (arg.endsWith("s")) {
                    count++;
                } else if (arg.endsWith("m")) {
                    count++;
                } else if (arg.endsWith("h")) {
                    count++;
                } else if (arg.endsWith("d")) {
                    count++;
                } else if (arg.endsWith("w")) {
                    count++;
                } else if (arg.endsWith("y")) {
                    count++;
                }

            }
        } else if (arg.length() > 2) {
            if (arg.endsWith("mo") && TimeUtil.isInt(arg.substring(0, arg.length() - 2))) {
                count++;
            }
        }
        return count;
    }

    public static boolean isUnitOfTime(String arg) {
        if (arg.length() > 1) {
            if (TimeUtil.isInt(arg.substring(0, arg.length() - 1))) {
                if (arg.endsWith("s")) {
                    return true;
                } else if (arg.endsWith("m")) {
                    return true;
                } else if (arg.endsWith("h")) {
                    return true;
                } else if (arg.endsWith("d")) {
                    return true;
                } else if (arg.endsWith("w")) {
                    return true;
                } else if (arg.endsWith("y")) {
                    return true;
                }

            }
        } else if (arg.length() > 2) {
            if (arg.endsWith("mo") && TimeUtil.isInt(arg.substring(0, arg.length() - 2))) {
                return true;
            }
        }

        int index = 0;
        while (!arg.equals("") && index < arg.length()) {
            char ch = arg.charAt(index);
            switch (ch) {
                case 's':
                    arg = arg.substring(index + 1);
                    break;
                case 'm':
                    if (arg.charAt(index + 1 >= arg.length() ? index : index + 1) == 'o') {
                        arg = arg.substring(index + 2);
                    } else {
                        arg = arg.substring(index + 1);
                    }
                    index = 0;
                    break;
                case 'h':
                    arg = arg.substring(index + 1);
                    index = 0;
                    break;
                case 'd':
                    arg = arg.substring(index + 1);
                    index = 0;
                    break;
                case 'w':
                    arg = arg.substring(index + 1);
                    index = 0;
                    break;
                case 'y':
                    arg = arg.substring(index + 1);
                    index = 0;
                    break;
                default:
                    if (!TimeUtil.isInt("" + ch)) {
                        return false;
                    }
            }
            index++;
        }
        return true;
    }

    public static String formatTime(long seconds) {
        String timeText = "";
        seconds += 1L;
        if (seconds % (60 * 60 * 24 * 365) >= 0) {
            int timecalc = (int) Math.floor(seconds / (60 * 60 * 24 * 365));
            seconds = seconds % (60 * 60 * 24 * 365);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += timecalc + " year,";
                } else {
                    timeText += timecalc + " years,";
                }
            }
        }

        if (seconds % (30 * 24 * 60 * 60) >= 0) {
            int timecalc = (int) Math.floor(seconds / (30 * 24 * 60 * 60));
            seconds = seconds % (30 * 24 * 60 * 60);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += " " + timecalc + " month,";
                } else {
                    timeText += " " + timecalc + " months,";
                }
            }
        }

        if (seconds % (60 * 60 * 24 * 7) >= 0) {
            int timecalc = (int) Math.floor(seconds / (60 * 60 * 24 * 7));
            seconds = seconds % (60 * 60 * 24 * 7);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += " " + timecalc + " week,";
                } else {
                    timeText += " " + timecalc + " weeks,";
                }
            }
        }

        if (seconds % (60 * 60 * 24) >= 0) {
            int timecalc = (int) Math.floor(seconds / (60 * 60 * 24));
            seconds = seconds % (60 * 60 * 24);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += " " + timecalc + " day,";
                } else {
                    timeText += " " + timecalc + " days,";
                }
            }
        }

        if (seconds % (60 * 60) >= 0) {
            int timecalc = (int) Math.floor(seconds / (60 * 60));
            seconds = seconds % (60 * 60);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += " " + timecalc + " hour,";
                } else {
                    timeText += " " + timecalc + " hours,";
                }
            }
        }

        if (seconds % 60 >= 0) {
            int timecalc = (int) Math.floor(seconds / (60));
            seconds = seconds % (60);
            if (timecalc != 0) {
                if (timecalc == 1) {
                    timeText += " " + timecalc + " minute,";
                } else {
                    timeText += " " + timecalc + " minutes,";
                }
            }
        }

        if (seconds > 0) {
            if (seconds == 1) {
                timeText += " " + seconds + " second,";
            } else {
                timeText += " " + seconds + " seconds,";
            }
        }
        if (timeText.length() > 0) {
            timeText = timeText.substring(0, timeText.length() - 1);
            int lastComma = timeText.lastIndexOf(",");
            if (lastComma != -1) {
                timeText = timeText.substring(1, lastComma) + " and " + timeText.substring(lastComma + 2, timeText.length());
            } else {
                timeText = timeText.substring(1);
            }
        } else {
            timeText = "0 seconds";
        }
        return timeText;
    }

    public static long convertToSeconds(String time) {
        time = time.toLowerCase();
        long seconds = 0;
        int index = 0;
        if (time.equals("") || !((time.contains("s") || time.contains("m") || time.contains("h") || time.contains("d") || time.contains("w") || time.contains("y")))) {
            return -1;
        }
        while (!time.equals("")) {
            char ch = time.charAt(index);
            switch (ch) {
                case 's':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    seconds += Integer.parseInt(time.substring(0, index));
                    if (time.length() > index + 1) {
                        time = time.substring(index + 1);
                        index = 0;
                    } else {
                        return seconds;
                    }
                    break;
                case 'm':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    if (time.charAt(index + 1 >= time.length() ? index : index + 1) == 'o') {
                        seconds += Integer.parseInt(time.substring(0, index)) * 30 * 24 * 60 * 60;
                        if (time.length() > index + 2) {
                            time = time.substring(index + 2);
                        } else {
                            return seconds;
                        }
                    } else {
                        seconds += Integer.parseInt(time.substring(0, index)) * 60;
                        if (time.length() > index + 1) {
                            time = time.substring(index + 1);
                        } else {
                            return seconds;
                        }
                    }
                    index = 0;
                    break;
                case 'h':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    seconds += Integer.parseInt(time.substring(0, index)) * 60 * 60;
                    if (time.length() > index + 1) {
                        time = time.substring(index + 1);
                        index = 0;
                    } else {
                        return seconds;
                    }
                    break;
                case 'd':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    seconds += Integer.parseInt(time.substring(0, index)) * 60 * 60 * 24;
                    if (time.length() > index + 1) {
                        time = time.substring(index + 1);
                        index = 0;
                    } else {
                        return seconds;
                    }
                    break;
                case 'w':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    seconds += Integer.parseInt(time.substring(0, index)) * 60 * 60 * 24 * 7;
                    if (time.length() > index + 1) {
                        time = time.substring(index + 1);
                        index = 0;
                    } else {
                        return seconds;
                    }
                    break;
                case 'y':
                    if (!TimeUtil.isInt(time.substring(0, index))) {
                        return -1;
                    }
                    seconds += Integer.parseInt(time.substring(0, index)) * 60 * 60 * 24 * 365;
                    if (time.length() > index + 1) {
                        time = time.substring(index + 1);
                        index = 0;
                    } else {
                        return seconds;
                    }
                    break;
            }
            index++;
        }
        return seconds;
    }

}
