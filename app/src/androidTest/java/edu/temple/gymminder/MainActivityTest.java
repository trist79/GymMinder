package edu.temple.gymminder;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() throws InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.emailField), isDisplayed()));
        Thread.sleep(1000);
        appCompatEditText.perform(replaceText("some_girl@test.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.passwordField), isDisplayed()));
        appCompatEditText2.perform(replaceText("tester1"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.signinButton), withText("Signin"), isDisplayed()));
        appCompatButton.perform(click());

        Thread.sleep(2000);

        ViewInteraction textView = onView(
                allOf(withText("hey p3p"),
                        childAtPosition(
                                withId(R.id.workoutsList),
                                0),
                        isDisplayed()));
        textView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.startTrackerButton), withText("start"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.startTrackerButton), withText("start"), isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.startTrackerButton), withText("start"), isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction button = onView(
                allOf(withText("Save workout results"), isDisplayed()));
        button.perform(click());

        pressBack();

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.add_workout_fab), isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.workoutNameEditText), isDisplayed()));
        appCompatEditText3.perform(replaceText("its a loe"), closeSoftKeyboard());

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.addExerciseButton), isDisplayed()));
        floatingActionButton2.perform(click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.repsEditText),
                        withParent(allOf(withId(R.id.exerciseCreatorItem),
                                childAtPosition(
                                        withId(R.id.exercisesListview),
                                        0))),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("3"), closeSoftKeyboard());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.setsEditText),
                        withParent(allOf(withId(R.id.exerciseCreatorItem),
                                childAtPosition(
                                        withId(R.id.exercisesListview),
                                        0))),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("3"), closeSoftKeyboard());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.finishExerciseButton), withText("Finish"), isDisplayed()));
        appCompatButton5.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Sign Out"), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.emailField), isDisplayed()));
        appCompatEditText6.perform(replaceText("1"), closeSoftKeyboard());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
