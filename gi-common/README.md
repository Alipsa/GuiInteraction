# gi-common

Common code and interfaces for the GuiInteraction library.

## Overview

This module contains:

- `GuiInteraction` - The core interface defining all UI interaction methods
- `AbstractInOut` - Abstract base class with shared implementations
- `FileUtils` - Utility methods for file and resource operations
- Support classes for clipboard operations

## Usage

This module is typically not used directly. Instead, use one of the implementation modules:

- [gi-fx](../gi-fx) - JavaFX implementation
- [gi-swing](../gi-swing) - Swing implementation
- [gi-console](../gi-console) - Console implementation

## Dependencies

This module includes:

- [Apache Tika](https://tika.apache.org/) - For content type detection
- [CommonMark](https://commonmark.org/) - For Markdown rendering
- [Matrix](https://github.com/Alipsa/matrix) - For table data structures

## API Documentation

See the [API Guide](../docs/API-Guide.md) for detailed usage examples.

## Maven Coordinates

```xml
<dependency>
    <groupId>se.alipsa.gi</groupId>
    <artifactId>gi-common</artifactId>
    <version>0.3.0</version>
</dependency>
```

## License

MIT License - see [LICENSE](../LICENSE)
