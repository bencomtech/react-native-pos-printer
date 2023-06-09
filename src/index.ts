import { DeviceEventEmitter, NativeModules, Platform } from 'react-native';
import type { Printer } from './model';
import {
  Command,
  PrinterConstants,
  printerCommand,
  printerTools,
} from './printerCommand';

type Callback = (devices: Printer[]) => void;

const isAndroid: boolean = Platform.OS === 'android';

class PrinterModule {
  public printerModule: any;
  private callback: Callback | null | undefined;
  private deviceEventEmitter: any;
  private isDebug: boolean = false;

  constructor() {
    this.printerModule = NativeModules.PosPrinter;
    this.log('Printer module is null ' + !!this.printerModule);
  }

  // initiate pos printer
  public init(isDebug: boolean = false): Promise<void> {
    this.isDebug = isDebug;

    if (!isAndroid) return Promise.reject('IOS is not supported');

    return this.printerModule.init();
  }

  // get all pos printer devices
  public getDevices(): Promise<Printer[]> {
    if (!isAndroid) return Promise.reject('IOS is not supported');

    return this.printerModule.getDevices();
  }

  // scan all pos printer devices
  public scanDevices(callback: Callback): Promise<boolean> {
    if (!isAndroid) return Promise.reject('IOS is not supported');

    this.log('start scanning devices');
    this.callback = callback;
    this.printerModule.scanDevices();
    this.listenToNativeEvent(true);

    return Promise.resolve(true);
  }

  // stop scannning
  public stopScanDevices(): Promise<boolean> {
    if (!isAndroid) return Promise.reject('IOS is not supported');

    this.callback = null;
    this.printerModule.stopScanningDevices();

    if (this.deviceEventEmitter) this.deviceEventEmitter.remove();

    this.log('stop scanning devices');

    return Promise.resolve(true);
  }

  // connect to pos printer device
  public connectDevice(
    deviceID: string,
    timeout: number = 30000
  ): Promise<any> {
    if (!isAndroid) return Promise.reject('IOS is not supported');
    if (this.deviceEventEmitter) this.deviceEventEmitter.remove();

    return this.printerModule.connectDevice(deviceID, timeout);
  }

  // printer all commands to printer
  public async printTestReceipt(storageUrl?: string) {
    const cmd: Command[] = [
      printerCommand.setPrinter(
        PrinterConstants.Command.ALIGN,
        PrinterConstants.Command.ALIGN_CENTER
      ),
      printerCommand.setFont(1, 0, 2, 0),
    ];

    cmd.push(printerCommand.printLine('RECEIPT TITLE'));
    cmd.push(printerCommand.printSeparator30('-----'));
    cmd.push(
      printerCommand.printKeyValue30(
        printerTools.generateKeyValuePair('', 'Price', 0),
        '50$'
      )
    );
    cmd.push(printerCommand.printLine(''));

    if (storageUrl) cmd.push(printerCommand.printImageFromStorage(storageUrl));

    cmd.push(printerCommand.printLine(''));
    cmd.push(printerCommand.setFont(-2, -2, 0, 0));
    cmd.push(printerCommand.printText('end of receipt'));
    cmd.push(printerCommand.printLine(''));

    try {
      await this.printerModule.addCommands(cmd);
      if (this.deviceEventEmitter) this.deviceEventEmitter.remove();
      return;
    } catch (e) {
      /** get rid of the linter error */
    }
  }

  // listen all changed state from native events
  private listenToNativeEvent(start: boolean): void {
    if (start) {
      this.deviceEventEmitter = DeviceEventEmitter.addListener(
        'available_bluetooth_devices',
        (devices: Printer[]) => {
          this.log('available devices' + JSON.stringify(devices));

          if (this.callback) this.callback(devices);
        }
      );

      return;
    }

    if (this.deviceEventEmitter) this.deviceEventEmitter.remove();
  }

  // log message error
  private log(message: string) {
    if (this.isDebug) {
      // tslint:disable-next-line:no-console
      console.log('POS Printer', message);
    }
  }
}

const PosPrinter: PrinterModule = new PrinterModule();

export { PosPrinter, Command, PrinterConstants, printerCommand, printerTools };
export default PosPrinter;
