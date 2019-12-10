# Android-DualBle
库的使用方法，demo里面，有具体的栗子，仔细阅读下，很多注释的，应该容易理解

　　1、建立自己的蓝牙设备对象，demo中有两种蓝牙设备，蓝牙控制led的设备(LedDevice)、蓝牙控制加热器设的备(HeaterDevice)，他们继承蓝牙库的对外设备（BLEAppDevice），添加自己的新特征，如led灯颜色，heater定时时间。蓝牙对象必须包含自己的服务、发送、接收三种uuid，以及自定义一个设备类型id，重写三个抽象方法，把uuid写进去。构造方法必须如下的方式，固定两个参数，并且调用父类的构造方法。

2、建立自己蓝牙设备的数据包结构对象（可选），继承DataParserAdapter，重写相应方法。框架内部根据你定义的结构，自动帮你把蓝牙回应的数据包提炼出来（主要是处理断包、粘包问题），最终的数据包通过onDeviceRespSpliceData(BLEPacket message)方法回调给你。当然你也可以不用架构的处理算法，自己拼包，在DataCircularBuffer 类中，pushOriginalDataToBuffer(byte[] originalData)方法，是各个蓝牙设备数据推过来的入口，在这里接入自己的算法。
　　如果不建立DataParserAdapter对象，则默认为null，蓝牙回应的数据，通过onDevicesRespOriginalData(BLEPacket message) 方法回调给你。

　　3、建立自己的蓝牙管理对象，继承BLEBaseManager，重写必要的、可选的方法。蓝牙的各种信息交换，都是通过这个类回调给你。很重要！仔细阅读BLEServerListener接口里的方法说明，重写自己需要的方法。  （1）必须重写 onGetDevicesServiceUUID（）方法，把自己定义的设备类型ID和设备的service uuid，用map写进去。框架连接上设备后，读取设备的service uuid，根据这个map分辨出是那种类型的设备。
      （2）必须重写BLEAppDevice onCreateDevice(BluetoothDevice bluetoothDevice, int deviceType)方法，框架识别设备类型后，回调给你，你根据设备类型，创建设备对象实例。
      （3）onAddScanDevice(BluetoothDevice bluetoothDevice)方法，框架扫描到设备，就会回调这个方法。
      （4）onAddNewDevice(BLEAppDevice device)方法，框架连接成功一个设备，各种状态完备后，回调这个方法。
这些方法在BLEServerListener接口都有详细说明
建立三个对象，就可以使用此框架了，如此简单！

　　4、初始化蓝牙框架，APP获得蓝牙相应权限后，调用BLEBaseManager的 initBle（..）方法初始化蓝牙。见demo
 注意

　　多设备同时工作，必定引起并发竞争问题，自己要做好同步！demo只是使用方法，没有处理那些问题
