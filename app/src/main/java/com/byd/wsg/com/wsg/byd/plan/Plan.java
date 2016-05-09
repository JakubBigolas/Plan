package com.byd.wsg.com.wsg.byd.plan;

import android.util.Log;

import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jakub on 2016-04-26.
 */
public class Plan implements Serializable {

    private TimeTable timeTable;
    private ArrayList<Interval> intervals;
    private Type type = Type.SHORT;

    public static enum Type {
        SHORT, FULLY;
    }


    /**
     * @param timeTable
     * @param intervals every interval must starts at end of previous interval
     * @throws IntervalsException is thrown when intervals continuity is broken
     */
    public Plan(TimeTable timeTable, ArrayList<Interval> intervals) throws IntervalsException {
        this.timeTable = timeTable;
        if (intervals != null) {
            ArrayList<Interval> newIntervals = new ArrayList<>();
            if (intervals.size() > 1) {
                for (int i = 0; i < intervals.size(); i++) {
                    Interval interval = intervals.get(i);
                    if (interval != null) {
                        for (int a = i + 1; a < intervals.size(); a++) {
                            Interval cur = intervals.get(a);
                            if (cur != null
                                    && cur.getType() == interval.getType()
                                    && cur.getStart() <= interval.getEnd()
                                    && cur.getEnd() >= interval.getStart()) {
                                interval = new Interval(
                                        Math.min(interval.getStart(), cur.getStart()),
                                        Math.max(interval.getEnd(), cur.getEnd()),
                                        interval.getType());
                                intervals.set(a, null);
                            }
                        }
                        newIntervals.add(interval);
                    }
                }
                intervals = newIntervals;
            }
            Collections.sort(intervals);
            if (intervals != null && !_checkContinuity(intervals))
                throw new IntervalsException();
        }
        this.intervals = intervals;
    }

    private boolean _checkContinuity(List<Interval> intervals) {
        if (intervals.size() == 1) return true;
        for (int i = 1; i < intervals.size(); i++)
            if (intervals.get(i).getStart() != intervals.get(i - 1).getEnd()) return false;
        return true;
    }

    public final boolean isShort() {
        return type == Type.SHORT;
    }

    public final boolean isFully() {
        return type == Type.FULLY;
    }


    public final Type getType() {
        return type;
    }

    public Plan switchTimeTable() {
        return isShort() ? switchTimeTableToFully() : switchTimeTableToShort();
    }

    /**
     * @param intervalPerMinute if null the default 1
     */
    public Plan switchTimeTableToFullyDay(Integer intervalPerMinute) {
        if (intervalPerMinute == null) intervalPerMinute = 1;
        else if (intervalPerMinute <= 0 || intervalPerMinute > 1440)
            throw new IllegalStateException("intervalPerMinute must be > 0 and <= 1440 minutes");

        ArrayList<Interval> intervals = new ArrayList<>();
        for (int i = 0; i < 1440; i += intervalPerMinute)
            intervals.add(new Interval(i, i + intervalPerMinute, Interval.Type.FREE));

        if (intervals.size() == 0) // gdy dla całego dnia
            intervals.add(new Interval(0, 1440, Interval.Type.FREE));
        else if (intervals.get(intervals.size() - 1).getEnd() < 1440)
            intervals.add(new Interval(intervals.get(intervals.size() - 1).getEnd(), 1440, Interval.Type.FREE));


        return null;
    }

    public Plan switchTimeTableToShort() {
        if (isShort())
            return this;
        if (timeTable != null) {
            ArrayList<DayTable> days = new ArrayList<>();
            for (DayTable day : timeTable) {

                ArrayList<Interval> intervals = day.getIntervalsClone();
                if (intervals.size() > 1) {
                    ArrayList<Interval> newIntervals = new ArrayList<>();
                    for (int i = 0; i < intervals.size(); i++) {
                        Interval interval = intervals.get(i);
                        if (interval != null && interval.getType() == Interval.Type.LESSON) {
                            Lesson l = (Lesson) interval;
                            for (int a = i + 1; a < intervals.size(); a++) {
                                Interval curInterval = intervals.get(a);
                                if (curInterval != null) {
                                    if (curInterval.getType() == Interval.Type.LESSON) {
                                        Lesson cur = (Lesson) curInterval;
                                        if (cur.getType().equals(l.getType())
                                                && cur.getRoom().equals(l.getRoom())
                                                && cur.getSubject().equals(l.getSubject())
                                                && cur.getStart() <= l.getEnd()
                                                && cur.getEnd() >= l.getStart()) {
                                            l = new Lesson(l.getStart(), cur.getEnd(), l.getRoom(), l.getSubject());
                                            intervals.set(a, null);
                                        }
                                    } else
                                        intervals.set(a, null);
                                }
                            }
                            newIntervals.add(l);
                        }
                    }
                    intervals = newIntervals;
                } else if (intervals.size() == 1 && intervals.get(1).getType() != Interval.Type.LESSON)
                    intervals = new ArrayList<>();

                ArrayList<Interval> lessons = new ArrayList<>();
                Lesson lesson = null;
                for (Interval i : intervals) {
                    try {
                        Lesson l = (Lesson) i;
                        if (lesson == null) { // GDY ZACZYNAMY LUB POPRZENIO BYLA PRZERWA
                            lesson = new Lesson(l.getStart(), l.getEnd(), l.getRoom(), l.getSubject());
                        } else if (lesson.getEnd() < l.getStart()) {
                            lessons.add(lesson);// GDY LEKCJA JEST INNA NIZ POPRZENIA LUB GDY BYLA POMIEDZY NIMI NIEJAWNA PRZERWA
                            lesson = new Lesson(l.getStart(), l.getEnd(), l.getRoom(), l.getSubject());
                            continue;
                        } else if (l.getRoom().equals(lesson.getRoom()) && l.getSubject().equals(lesson.getSubject())) { // GDY LEKCJA JEST KONTYUOWANA
                            lesson = new Lesson(lesson.getStart(), l.getEnd(), lesson.getRoom(), lesson.getSubject());
                        }
                    } catch (ClassCastException e) {
                        if (lesson != null) // GDY NIE-LEKCJA
                            lessons.add(lesson);
                        lesson = null;
                    }
                }
                if (lesson != null)// DODAJ OSTATNIĄ JESLI BYLA LEKCJĄ
                    lessons.add(lesson);
                days.add(new DayTable(day.getDate(), lessons));
            }
            return new Plan(new TimeTable(days), this.intervals);
        }
        return null;
    }

    private Lesson _findNextLesson(Iterator<Interval> it) {
        while (it.hasNext()) {
            Interval i = it.next();
            if (i.getType() == Interval.Type.LESSON)
                return (Lesson) i;
        }
        return null;
    }

    public Plan switchTimeTableToFully() {
        if (isFully())
            return this;
        else if (isShort()) {
            if (intervals != null && timeTable != null && !intervals.isEmpty()) {
                ArrayList<DayTable> days = new ArrayList<>();
                for (DayTable day : timeTable) {
                    ArrayList<Interval> intervals = new ArrayList<>();

                    Iterator<Interval> it = day.iterator(); // PRZEJSCIE DO PIERWSZEJ LEKCJI W ZAKRESIE
                    Lesson l = _findNextLesson(it);
                    Interval i = null;
                    int intervalIndex = 0;
                    boolean canAddL = true;
                    while (intervalIndex < this.intervals.size()) {
                        if (i == null) i = this.intervals.get(intervalIndex);

                        while (l != null && l.getEnd() <= i.getStart()) { // JESLI LEKCJA SKONCZYLA SIE ZANIM ZACZAL SIE INTERWAL LUB GDZY NIE JEST LEKCJA
                            if (canAddL) intervals.add(l);
                            l = _findNextLesson(it);
                            canAddL = true;
                        }

                        if (l == null) { // JEST SKONCZYLY SIE LEKCJE DODAJ WOLNE
                            intervals.add(new Interval(i.getStart(), i.getEnd(), _checkIfBreakOrReturnFree(i)));
                            for (++intervalIndex; intervalIndex < this.intervals.size(); intervalIndex++) {
                                i = this.intervals.get(intervalIndex);
                                intervals.add(new Interval(i.getStart(), i.getEnd(), _checkIfBreakOrReturnFree(i)));
                            }
                            break;
                        }

                        if (l.getStart() >= i.getEnd()) { // JESLI LEKCJA ZACZYNA SIE PO INTERWALE
                            intervals.add(new Interval(i.getStart(), i.getEnd(), _checkIfBreakOrReturnFree(i)));
                            i = null;
                            intervalIndex++;
                            continue;
                        }

                        int startOffset = l.getStart() - i.getStart(); // PRZESUNIECIE LEKCI WZGLEDEM POCZATKU INTERWALU (+)
                        int endOffset = i.getEnd() - l.getEnd(); // PRZESUNIECIE LEKCI WZGLEDEM KONCA INTERWALU (+)

                        if (startOffset > 0) // GDY LEKKCJA ZACZYNA SIE POZNIEJ DODAJ CZAS WOLNY PRZED
                            intervals.add(new Interval(i.getStart(), l.getStart(), _checkIfBreakOrReturnFree(i)));

                        if (canAddL)
                            intervals.add(l); // DODAJ LEKCJĘ

                        if (endOffset < 0) {
                            i = null;
                            canAddL = false;
                            intervalIndex++;
                        } else if (endOffset == 0) {
                            i = null;
                            intervalIndex++;
                            l = _findNextLesson(it);
                            canAddL = true;
                        } else {
                            i = new Interval(l.getEnd(), i.getEnd(), i.getType());
                            canAddL = true;
                            l = _findNextLesson(it);
                        }
//                        l = _findNextLesson(it);
                    }
                    days.add(new DayTable(day.getDate(), intervals));
                }

                Plan plan = new Plan(new TimeTable(days), (ArrayList<Interval>) intervals.clone());
                plan.type = Type.FULLY;
                return plan;
            }
        } else {
            Plan p = this.switchTimeTableToShort();
            if (p != null) return p.switchTimeTableToFully();
        }
        return null;
    }

    private Interval.Type _checkIfBreakOrReturnFree(Interval i) {
        return i.getType() == Interval.Type.BREAK ? Interval.Type.BREAK : Interval.Type.FREE;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }
}
