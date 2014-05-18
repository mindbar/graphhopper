package com.graphhopper.mobilius;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * @author mishadoff
 */
public final class InstructionService {

    private static InstructionService INSTANCE = new InstructionService();

    private String[] urlList;

    private InstructionService() {}

    public static InstructionService getInstance() {
        return INSTANCE;
    }

    private static final String CONTINUE = "continue";
    private static final String LEFT = "left";
    private static final String SLIGHT_LEFT = "slight_left";
    private static final String SHARP_LEFT = "sharp_left";
    private static final String RIGHT = "right";
    private static final String SLIGHT_RIGHT = "slight_right";
    private static final String SHARP_RIGHT = "sharp_right";
    private static final String OTHER = "other";

    public void setUrls(String[] urls) {
        urlList = urls;
    }

    public String getInstructions(Instruction i, double distance) {
        String name;
        String dir;
        if (i == null) {
            name = "stop";
            dir = "other";
        } else {
            name = i.getName();
            dir = getDir(i.getSign());
        }
        return "dir=" + dir + "&name=" + encode(name) + "&distance=" + distance;
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding");
        }
    }

    private String getDir(int sign) {
        switch (sign) {
            case Instruction.TURN_LEFT: return LEFT;
            case Instruction.TURN_SLIGHT_LEFT: return SLIGHT_LEFT;
            case Instruction.TURN_SHARP_LEFT: return SHARP_LEFT;
            case Instruction.TURN_RIGHT: return RIGHT;
            case Instruction.TURN_SLIGHT_RIGHT: return SLIGHT_RIGHT;
            case Instruction.TURN_SHARP_RIGHT: return SHARP_RIGHT;
            default: return OTHER;
        }
    }

    public void sendInstruction(InstructionList insList) {
        // select first non-other list
        Instruction ret = null;
        double distanceSum = 0;
        if (!insList.isEmpty()) {
            for (Instruction i : insList) {
                if (OTHER.equals(getDir(i.getSign()))) {
                    distanceSum += i.getDistance();
                    continue;
                } else {
                    ret = i;
                    break;
                }
            }
        }

        //
        String query = "?" + getInstructions(ret, distanceSum);

        if (urlList != null && urlList.length > 0) {
            for (String url : urlList) {
                if (url != null && url.trim().startsWith("http")) {
                    try {
                        get(url.trim() + query);
                    } catch (IOException e) {
                        // NOOOOO!
                    }
                }
            }
        }
    }

    private void get(String query) throws IOException {
        HttpURLConnection connection;
        BufferedReader rd = null;
        ArrayList<String> strings = new ArrayList<String>();
        try {
            URL url = new URL(query);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                strings.add(line);
            }
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }

}
