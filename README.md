# Development of Activity Recognition Application for Smartphone 
Activity Recognition application for smartphone collects sensory data only from accelerometer and extract features from it. Then classify the activities using Support Vector Machine (SVM) with Sequential Minimal Optimization (SMO). The purpose of using SVM with SMO was to solve the quadratic programming problem that we were facing while training of SVM and activity recognition with other classifiers. 
The raw sensory data, extracted feature data and activity label are stored inside the smartphone as a text file. The activity recognition application processed the user activity on per second basis. But it stores and display the result on the screen when an activity change is identified. In this way the user will not see the same activity repeatedly. The description of each class is described as follows:

i)	MainActivity:
This class is the main body of the application, which handles the overall process. It turns on and off the accelerometer, manages the file creation, feature extraction, classification, retrieving the decision, and Bluetooth communication. The sensory data are collected every second with 50Hz.

ii)	BluetoothChatService:
This class deals with the Bluetooth chat between smartphone and smartwatch, based on the Android Open Source Project. We have customized the code in such a way that it receives the data in Json format and then send the handler message to main activity based on its message type.

iii)	DeviceListActivity:
This class shows the list of available Bluetooth devices.

iv)	FileWrite:
This class creates a folder, writes all the raw sensory data files and feature data files

v)	FeatureExtraction:
This class extract features from the data. The extracted features are average, max, min, standard deviation, mean crossing, quartile, mean of absolute element (meanabs), and variance. 

vi)	Classification:
This class classifies the activities, which includes sit, stand, lie, and walk. When the classifier is set by the main activity, it trains the classifier based on the preserved feature vector with activity labels.

vii)	MessageType:
This class contains message type enums (Start, Stop, Data).
a)	Start: When the application receives a “Start” message, it starts the activity recognition process on the smartphone.
b)	Stop: When the application receives a “Stop” message, it stops the activity recognition process on the smartphone.
c)	Data: The “Data” message shows that the message contains sensory data. The smartphone collects the accelerometer data and store it into a text file. 

viii)	SensorObject:
This class is responsible for saving, getting, and setting of sensory data.
