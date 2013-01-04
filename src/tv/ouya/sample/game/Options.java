/*
 * Copyright (C) 2012 OUYA, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.ouya.sample.game;

public class Options {
    static private Options instance = new Options();

    static public Options getInstance() {
        return instance;
    }

    enum Level {
        FREEDOM,
        ALLEYWAY,
        BOXY,
    }
    private Level level = Level.FREEDOM;

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    private Options() {
    }
}
