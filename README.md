# auctionsniper
AuctionSniper application from [Growing Object Oriented Software, Guided By Tests (GOOS)](http://www.growing-object-oriented-software.com/)

## Openfire setup
1. Start the Openfire server
```sh
sh ./start-openfire.sh
```
2. Open http://localhost:9090 and go through the setup process
3. Login as admin
4. Create the test user

### Common failures

#### macos acessibility permission

You need to give accessibility permissions to the tool you are using to run the tests so that windowlicker can perform the gestures.
![2021-07-24_05-37](https://user-images.githubusercontent.com/9938253/126862931-da5b09f3-b81c-4d92-8a9b-55ffd6e1ca5a.png)
