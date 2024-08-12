// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2024T2, Assignment 3
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.util.*;
import java.io.*;

/**
 * Simulation of a Hospital ER
 * 
 * The hospital has a collection of Departments, including the ER department, each of which has
 *  and a treatment room.
 * 
 * When patients arrive at the hospital, they are immediately assessed by the
 *  triage team who determine the priority of the patient and (unrealistically) a sequence of treatments 
 *  that the patient will need.
 *
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 *
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl {

    /**
     * The map of the departments.
     * The names of the departments should be "ER", "X-Ray", "MRI", "UltraSound" and "Surgery"
     * The maximum patients should be 8 for "ER", 3 for "X-Ray", 1 for "MRI", 2 for "UltraSound" and
     * 3 for "Surgery"
     */

    private Map<String, Department> departments = new HashMap<String, Department>();
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private static final int MAX_PATIENTS = 5;   // max number of patients currently being treated
    private final Set<Patient> treatmentRoom = new HashSet<Patient>();
    public boolean inPriorityQue = false;

    // Copy the code from HospitalERCore and then modify/extend to handle multiple departments
    /*# YOUR CODE HERE */
    double numFinTreatment = 0.0;
    double totalTimeInWaiting = 0.0;
    double totalTimeInTreatment = 0.0;
    double tot1treated = 0.0;
    double tot1timewaiting = 0.0;

    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick


    /**
     * Reset the simulation:
     * stop any running simulation,
     * reset the waiting and treatment rooms
     * reset the statistics.
     */
    public void reset(boolean usePriorityQueue) {
        running = false;
        UI.sleep(2 * delay);  // to make sure that any running simulation has stopped

        time = 0;           // set the "tick" to zero.
        // reset the waiting room, the treatment room, and the statistics.
        /*# YOUR CODE HERE */
        waitingRoom.clear();
        treatmentRoom.clear();
        if (usePriorityQueue) {
            waitingRoom = new PriorityQueue<>();
            inPriorityQue = true;
        } else {
            waitingRoom = new ArrayDeque<>();
            inPriorityQue = false;
        }
        //statistics clear
        numFinTreatment = 0.0;
        totalTimeInWaiting = 0.0;
        totalTimeInTreatment = 0.0;
        tot1treated = 0.0;
        tot1timewaiting = 0.0;
        //visual clear
        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */

    public void run() {
        if (running) {
            return;
        } // don't start simulation if already running one!
        running = true;
        initialiseDepartments();
        while (running) {         // each time step, check whether the simulation should pause.

            // Hint: if you are stepping through a set, you can't remove
            //   items from the set inside the loop!
            //   If you need to remove items, you can add the items to a
            //   temporary list, and after the loop is done, remove all
            //   the items on the temporary list from the set.
            time++;
            /*# YOUR CODE HERE */
            List<Patient> compTreatment = new ArrayList<>();
            for (Patient p : treatmentRoom) {
                if (p.currentTreatmentFinished()) {
                    compTreatment.add(p);
                    numFinTreatment++;
                    totalTimeInTreatment += p.getTotalTreatmentTime();
                    if (p.getPriority() == 1) {
                        tot1treated++;
                        tot1timewaiting += p.getTotalWaitingTime();
                    }
                } else {
                    p.advanceCurrentTreatmentByTick();
                }
            }
            treatmentRoom.removeAll(compTreatment);
            for (Patient allPatients : waitingRoom) {
                allPatients.waitForATick();
            }


            if (treatmentRoom.size() < MAX_PATIENTS && !waitingRoom.isEmpty()) {
                treatmentRoom.add(waitingRoom.poll());
            }

            // Gets any new patient that has arrived and adds them to the waiting room
            Patient newPatient = PatientGenerator.getNextPatient(time);
            if (newPatient != null) {
                UI.println(time + ": Arrived: " + newPatient);
                waitingRoom.offer(newPatient);
            }
            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     */
    public void reportStatistics() {
        /*# YOUR CODE HERE */
        double tot1wait = 0.0;
        for (Patient p : waitingRoom) {
            totalTimeInWaiting += p.getTotalWaitingTime();
            for (Patient p2 : treatmentRoom) {
                if (p.getPriority() == 1 && p != p2) {
                    tot1timewaiting += p.getTotalWaitingTime() + p2.getTotalWaitingTime();
                    tot1wait++;
                }
            }
        }
        double totalTime = totalTimeInTreatment + totalTimeInWaiting;
        double totAvgWaitingTime = totalTime / (waitingRoom.size() + numFinTreatment);
        double tot1ever = tot1wait + tot1treated;
        double totTime1wating = tot1timewaiting / tot1ever; // doesnt account for those in the waiting room find num of pri 1 in waiting -- does now?
        UI.println("average total waiting = " + totAvgWaitingTime);
        UI.println("number of treatments = " + numFinTreatment);
        UI.println("total Pri 1 treated = " + tot1treated);
        UI.println("total pri 1 waiting  = " + totTime1wating);
    }

    public void initialiseDepartments(){
        // create each new department with values,
        // add each department to the map of departments
        Department ER = new Department("ER",8,inPriorityQue);
        Department XRay = new Department("X-Ray",3,inPriorityQue);
        Department MRI = new Department("MRI",1,inPriorityQue);
        Department UltraSound = new Department("UltraSound",2,inPriorityQue);
        Department Surgery = new Department("Surgery",3,inPriorityQue);
        departments.put("ER", ER);
        departments.put("X-Ray", XRay);
        departments.put("NRI", MRI);
        departments.put("UltraSound", UltraSound);
        departments.put("Surgery", Surgery);
    }
    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI() {
        UI.addButton("Reset (Queue)", () -> {
            this.reset(false);
        });
        UI.addButton("Reset (Pri Queue)", () -> {
            this.reset(true);
        });
        UI.addButton("Start", () -> {
            if (!running) {
                run();
            }
        });   //don't start if already running!
        UI.addButton("Pause & Report", () -> {
            running = false;
        });
        UI.addSlider("Speed", 1, 400, (401 - delay), (double val) -> {
            delay = (int) (401 - val);
        });
        UI.addSlider("Av arrival interval", 1, 50, PatientGenerator.getArrivalInterval(),
                PatientGenerator::setArrivalInterval);
        UI.addSlider("Prob of Pri 1", 1, 100, PatientGenerator.getProbPri1(),
                PatientGenerator::setProbPri1);
        UI.addSlider("Prob of Pri 2", 1, 100, PatientGenerator.getProbPri2(),
                PatientGenerator::setProbPri2);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000, 600);
        UI.setDivider(0.5);
    }

    /**
     * Redraws all the patients and the state of the simulation
     */
    public void redraw() {
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0, 32, 400, 32);

        // Draw the treatment room and the waiting room:
        double y = 80;
        UI.setFontSize(14);
        UI.drawString("ER", 0, y - 35);
        double x = 10;
        UI.drawRect(x - 5, y - 30, MAX_PATIENTS * 10, 30);  // box to show max number of patients
        for (Patient p : treatmentRoom) {
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for (Patient p : waitingRoom) {
            p.redraw(x, y);
            x += 10;
        }
        UI.drawLine(0, y + 2, 400, y + 2);
    }


    /**
     * main:  Construct a new HospitalERCore object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments) {
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }
}

