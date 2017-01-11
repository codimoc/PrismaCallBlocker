# PrismaCallBlocker
Android Call Blocker with pattern management

* [Call for help](https://github.com/ConteDiMonteCristo/PrismaCallBlocker/wiki/I-need-your-help)
* [Help pages](https://github.com/ConteDiMonteCristo/PrismaCallBlocker/wiki/Help) 


## License
Copyright 2016 ConteDiMonteCristo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Changes in version 1.2
* Enforce compatibility with API 23 (Marshmallow)
* Rules can be exported to file (binary serialization) and imported/merged into a different installation
* The call detecting service now runs in the foreground
* Some  devices, like Huawei devices, regularly kill apps when the screen goes off. To prevent the service
to be killed, there is an alert dialog for putting PrismaCallBlocker in the white list (protected app)
* Link to FAQ web page
