package automation.suite.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TableReport {
    /*
     * Creating a custom HTML Table Report
     * Data feeding from Cucumber JSON File
     * @author Dorin Onofrei
     */

    // Main Json Array
    static JSONArray allScenarioData = new JSONArray();
    static ArrayList<LinkedHashMap<String, String>> resultExecution = new ArrayList<>();


    public static void createHTML_Report(){
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("BDD/dswb_post_build_db_test.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONArray scenarios = (JSONArray) obj;

            // Number of Scenarios
            int numberScenarios = getTotalNumberOfScenarios(scenarios);

            // Store data in JSON
            for (int i = 0; i < numberScenarios; i++) {
                JSONObject tempObj = new JSONObject();
                tempObj.put("Scenario_Name", getScenarioName(i, scenarios));
                tempObj.put("Step", getStepName(i, scenarios));
                tempObj.put("Result", getStatus(i, scenarios));
                tempObj.put("Error", getError(i, scenarios));
                allScenarioData.add(tempObj);
            }


            //// Store data in List HashMap
            for (int i = 0; i < numberScenarios; i++) {
                LinkedHashMap<String, String> keyVal = new LinkedHashMap<>();
                keyVal.put("Scenario_Name", getScenarioName(i, scenarios));
                keyVal.put("Step", getStepName(i, scenarios));
                keyVal.put("Result", getStatus(i, scenarios));
                keyVal.put("Error", getError(i, scenarios));
                resultExecution.add(keyVal);
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        generateHTML();
    }


    private static int getTotalNumberOfScenarios(JSONArray jsonArray) {
        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
        return ((JSONArray) jsonObject.get("elements")).size();
    }

    private static String getScenarioName(int index, JSONArray scenarios) {
        JSONObject jsonObject = (JSONObject) scenarios.get(0);
        JSONArray jsonArray = (JSONArray) jsonObject.get("elements");
        return (String) ((JSONObject) jsonArray.get(index)).get("name");

    }

    private static String getStatus(int index, JSONArray scenarios) {
        JSONObject jsonObject = (JSONObject) scenarios.get(0);
        JSONArray jsonArray = (JSONArray) jsonObject.get("elements");
        JSONArray steps = (JSONArray) ((JSONObject) jsonArray.get(index)).get("steps");
        JSONObject result = (JSONObject) ((JSONObject) steps.get(0)).get("result");
        return (String) result.get("status");
    }

    private static String getStepName(int index, JSONArray scenarios) {
        JSONObject jsonObject = (JSONObject) scenarios.get(0);
        JSONArray jsonArray = (JSONArray) jsonObject.get("elements");
        JSONArray steps = (JSONArray) ((JSONObject) jsonArray.get(index)).get("steps");
        return (String) ((JSONObject) steps.get(0)).get("name");
    }

    private static String getError(int index, JSONArray scenarios) {
        JSONObject jsonObject = (JSONObject) scenarios.get(0);
        JSONArray jsonArray = (JSONArray) jsonObject.get("elements");
        JSONArray steps = (JSONArray) ((JSONObject) jsonArray.get(index)).get("steps");
        JSONObject result = (JSONObject) ((JSONObject) steps.get(0)).get("result");
        String errorMessage = (String) result.get("error_message");
        if (errorMessage == null)
            return errorMessage = "";
        return errorMessage;
    }

    private static void generateHTML() {
        String date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
        String time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());


        File htmlTemplateFile = new File("BDD/Report_Template.html");
        String htmlString = null;
        try {
            htmlString = FileUtils.readFileToString(htmlTemplateFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String title = "Test Execution finished on " + date + " at " + time;
        String body = "\n" +
                "\n<h2>" + title + "</h2>" +
                "<table class=\"tg\">\n" +
                "<thead>\n" +
                "  <tr>\n";
        /// Adding table headers
        for (LinkedHashMap<String, String> stringStringHashMap : resultExecution) {
            for (String nameColumn : stringStringHashMap.keySet()) {
                if (!body.contains(nameColumn))
                    body += " <th class=\"tg-3wb8\"><b>" + nameColumn + "</b></th>";
            }
        }
        /// closing table headers
        body += "  </tr>\n" +
                "</thead>\n<tbody>";

        /// Adding table data
        for (LinkedHashMap<String, String> stringStringHashMap : resultExecution) {
            body += "<tr>";
            for (String valueRow : stringStringHashMap.values()) {
//                if (!body.contains(valueRow))

                if (valueRow.equals("passed")) {
                    body += "<td class=\"tg-0pky\" style=\"background-color:rgb(147,196,125);\"><b>" + StringUtils.capitalize(valueRow) + "</b></td>";
                } else if (valueRow.equals("failed")) {
                    body += "<td class=\"tg-0pky\" style=\"background-color:rgb(255,0,0);\"><b>" + StringUtils.capitalize(valueRow) + "</b></td>";
                }else {
                    body += "<td class=\"tg-0pky\">" + valueRow + "</td>";
                }
            }

            body += "</tr>";
        }
        body +=
                "</tbody>\n" +
                        "</table>";


        htmlString = htmlString.replace("$title", title);
        htmlString = htmlString.replace("$body", body);
        File newHtmlFile = new File("BDD/Table_Report.html");
        try {
            FileUtils.writeStringToFile(newHtmlFile, htmlString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
