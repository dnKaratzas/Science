package com.example.android.science.utilities;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.science.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristina on 09/08/2018.
 * Helper methods related to requesting and receiving API data.
 */
public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the data set and return a List of {@link Question}.
     */
    public static List<Question> fetchQuestionsListData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request", e);
        }

        // Extract relevant fields from the JSON response and return it
        return extractListDataFromJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // TODO check response code
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the whole JSON response from
     * the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Question} objects that has been built up from parsing a JSON response.
     */
    private static List<Question> extractListDataFromJson(String stringJSON) {
        /* Key value for the "results" array.*/
        final String RESULTS_KEY = "results";

        /* Key value for the cake "question" string.*/
        final String QUESTION_KEY = "question";

        /* Key value for the "correct_answer" string.*/
        final String CORRECT_KEY = "correct_answer";

        /* Key value for the "incorrect_answers" array.*/
        final String INCORRECT_KEY = "incorrect_answers";

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(stringJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding questions to
        List<Question> questions = new ArrayList<>();
        try {
            JSONObject response = new JSONObject(stringJSON);
            if (response.has(RESULTS_KEY)) {
                JSONArray results = response.getJSONArray(RESULTS_KEY);
                // Loop through each feature in the array
                for (int i = 0; i < results.length(); i++) {
                    // Create an empty Question Object so that we can start adding information about it
                    Question question = new Question();

                    // Get the question JSONObject at position i
                    JSONObject questionObject = results.getJSONObject(i);

                    // Extract question title
                    if (questionObject.has(QUESTION_KEY)) {
                        String questionTitle = questionObject.getString(QUESTION_KEY);
                        question.setQuestionTitle(questionTitle);
                    }

                    // Extract correct answer
                    if (questionObject.has(CORRECT_KEY)) {
                        String correctAnswer = questionObject.getString(CORRECT_KEY);
                        question.setCorrectAnswer(correctAnswer);
                    }

                    // Extract incorrect answers array
                    if (questionObject.has(INCORRECT_KEY)) {
                        JSONArray incorrectAnswers = questionObject.getJSONArray(INCORRECT_KEY);
                        List<String> incorrectArray = new ArrayList<>();
                        for (int j = 0; j < incorrectAnswers.length(); j++) {
                            incorrectArray.add(incorrectAnswers.getString(i));
                        }
                        question.setIncorrectAnswers(incorrectArray);
                    }
                    // Add question Object to list of questions
                    questions.add(question);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        // Return the list of questions
        return questions;
    }
}
