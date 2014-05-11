SemEval2014 TASK A/B
====================
This distills everything we know from the Workflow, but as the scikit-learn LinearSVC classifier 
seems to be broken and time's tight, I've rebased the whole thing around Weka/Java for easier 
testing, nice debugging etc

Subjectivity currently works well, approx 93% accuracy (baseline around 90%).

See todo.txt for tasks.

* Run `sh installJars.sh` to install the custom GimpelPosTagger into your local Maven repo
* Use `mvn eclipse:configure-workspace -Dworkspace=PATH_TO_YOUR_WORKSPACE` to configure Eclipse
* Use `mvn eclipse:eclipse` to generate the eclipse project

Now constitutes part A and B of our SemEval submission.
