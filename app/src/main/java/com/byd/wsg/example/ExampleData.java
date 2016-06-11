package com.byd.wsg.example;

import android.util.JsonReader;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.Interval;
import com.byd.wsg.com.wsg.byd.plan.Plan;
import com.byd.wsg.com.wsg.byd.plan.TimeTable;
import com.byd.wsg.com.wsg.byd.plan.raw.RawData;
import com.byd.wsg.com.wsg.byd.plan.raw.RawInterval;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTimeTable;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jakub on 2016-04-25.
 */
public class ExampleData {

    public static final String TAG = "LOG-ExampleData";

    public static Plan prepareExampleTimeTable() throws ParseException {
        Gson gson = new Gson();
        RawData rtt = gson.fromJson(exampleTimeTableString, RawData.class);
        Log.d(TAG, "prepareExampleTimeTable: " + rtt);

        ArrayList<Interval> intervals = null;
        if(rtt.intervals != null){
            intervals = new ArrayList<>(rtt.intervals.size());
            for(RawInterval i : rtt.intervals)
                intervals.add(new Interval(i.getStart(),i.getEnd(),i.getTypeEnum()));
        }


        return new Plan(
               null,// new TimeTable(rtt.timetable),
                intervals
        );
    }

    public static final String exampleTimeTableString = "{\n" +
            "  intervals: [{\n" +
            "    start: 450,\n" +
            "    end: 540,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 540,\n" +
            "    end: 555,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 555,\n" +
            "    end: 645,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 645,\n" +
            "    end: 660,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 660,\n" +
            "    end: 750,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 750,\n" +
            "    end: 765,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 765,\n" +
            "    end: 855,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 855,\n" +
            "    end: 870,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 870,\n" +
            "    end: 960,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 960,\n" +
            "    end: 975,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 975,\n" +
            "    end: 1065,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 1065,\n" +
            "    end: 1080,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 1080,\n" +
            "    end: 1170,\n" +
            "    type: 'lesson'\n" +
            "  }, {\n" +
            "    start: 1170,\n" +
            "    end: 1175,\n" +
            "    type: 'break'\n" +
            "  }, {\n" +
            "    start: 1175,\n" +
            "    end: 1265,\n" +
            "    type: 'lesson'\n" +
            "  }],\n" +
            "  timetable: {\n" +
            "    from: '29-04-2016',\n" +
            "    to: '05-05-2016',\n" +
            "    days: [{\n" +
            "      title: 'Poniedziałek',\n" +
            "      lessons: []\n" +
            "    }, {\n" +
            "      title: 'Wtorek',\n" +
            "      lessons: [{\n" +
            "        title: 'Systemy wbudowane',\n" +
            "        start: 555,\n" +
            "        end: 645,\n" +
            "        with: 'dr inż. Jerzy Bakalarczyk',\n" +
            "        room: 'F2',\n" +
            "        type: 'W'\n" +
            "      }, {\n" +
            "        title: 'Edycja i prezentacja danych',\n" +
            "        start: 660,\n" +
            "        end: 750,\n" +
            "        with: 'mgr inż. Hanna Tużylak',\n" +
            "        room: 'B-P6',\n" +
            "        type: 'L'\n" +
            "      }, {\n" +
            "        title: 'Systemy wbudowane',\n" +
            "        start: 870,\n" +
            "        end: 960,\n" +
            "        with: 'dr inż. Jerzy Bakalarczyk',\n" +
            "        room: 'B-P7',\n" +
            "        type: 'L'\n" +
            "      }, {\n" +
            "        title: 'Przedmiot branżowy',\n" +
            "        start: 961,\n" + // 17:45
            "        end: 1065,\n" + // 18:00
            "        with: 'inż. Tomasz Wnuk',\n" +
            "        room: 'B-P6',\n" +
            "        type: 'L'\n" +
            "      }, {\n" +
            "        title: 'Przedmiot branżowy',\n" +
            "        start: 1021,\n" + // 1080
            "        end: 1171,\n" + // 1170
            "        with: 'inż. Tomasz Wnuk',\n" +
            "        room: 'B-P6',\n" +
            "        type: 'L'\n" +
            "      }]\n" +
            "    }, {\n" +
            "      title: 'Środa',\n" +
            "      lessons: []\n" +
            "    }, {\n" +
            "      title: 'Czwartek',\n" +
            "      lessons: [{\n" +
            "        title: 'Etyka',\n" +
            "        start: 765,\n" +
            "        end: 855,\n" +
            "        with: 'dr Daniel Żuromski',\n" +
            "        room: 'A2',\n" +
            "        type: 'W'\n" +
            "      }]\n" +
            "    }, {\n" +
            "      title: 'Piątek',\n" +
            "      lessons: [{\n" +
            "        title: 'Programowanie PLC',\n" +
            "        start: 555,\n" +
            "        end: 645,\n" +
            "        with: 'mgr inż. Tomasz Ocetkiewicz',\n" +
            "        room: 'B-P7',\n" +
            "        type: 'L'\n" +
            "      }, {\n" +
            "        title: 'Programowanie aplikacji na urządzenia mobilne (Java)',\n" +
            "        start: 660,\n" +
            "        end: 750,\n" +
            "        with: 'mgr inż. Tomasz Ocetkiewicz',\n" +
            "        room: 'B-P6',\n" +
            "        type: 'L'\n" +
            "      }, {\n" +
            "        title: 'Konsultacje dyplomowe',\n" +
            "        start: 765,\n" + //765
            "        end: 855,\n" +
            "        with: 'dr inż. Jacek Gospodarczyk',\n" +
            "        room: 'B-P6',\n" +
            "        type: 'C'\n" +
            "      }]\n" +
            "    }, {\n" +
            "      title: 'Sobota',\n" +
            "      lessons: []\n" +
            "    }, {\n" +
            "      title: 'Niedziela',\n" +
            "      lessons: []\n" +
            "    }]\n" +
            "  }\n" +
            "}\n";

}
