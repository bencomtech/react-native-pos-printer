# react-native-pos-printer

react native module for printing receipt on pos printer. (only support for android)

## Installation

```sh
npm install react-native-pos-printer
```

## Usage

```js
import PosPrinter from 'react-native-pos-printer';
```

### Init Printer

Initializes printer, the return will be Promise<void>

```js
PosPrinter.init(isDebug?)
    .then((res) => console.log(res))
    .catch((err) => console.log(err));
```

### Get Devices

Get all printer devices, the return will be Promise<Printer[]>

```js
PosPrinter.getDevices()
  .then((device) => console.log(device))
  .catch((err) => console.log(err));
```

### Scan Devices

Scan all devices printer, the return will be Promise<boolean>

```js
PosPrinter.scanDevices()
  .then((res) => console.log(res))
  .catch((err) => console.log(err));
```

### Stop Scan Devices

Stop scanning all devices printer, the return will be Promise<boolean>

```js
PosPrinter.stopScanDevices()
  .then((res) => console.log(res))
  .catch((err) => console.log(err));
```

### Connect Device

Connect to specific pos printer, the return will be Promise<any>

```js
PosPrinter.connectDevice(deviceId, timeout)
  .then((res) => console.log(res))
  .catch((err) => console.log(err));
```

## Print Test Receipt

Test print on pos printer

```js
PosPrinter.printTestReceipt(storageUrl?)
    .then((res) => console.log(res))
    .catch((err) => console.log(err));
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
