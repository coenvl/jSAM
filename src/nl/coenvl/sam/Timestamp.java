/**
 * File Timestamp.java
 *
 * This file is part of the jSAM project 2014.
 * 
 * Copyright 2014 Coen van Leeuwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.coenvl.sam;

import java.util.ArrayList;

/**
 * Timestamp
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 apr. 2014
 * 
 */
public class Timestamp implements Comparable<Timestamp> {

	ArrayList<Integer> counters;

	public Timestamp() {
		counters = new ArrayList<Integer>();
	}

	public void add(int index) {
		if (index < counters.size())
			counters.set(index, counters.get(index) + 1);
		else
			while (counters.size() <= index)
				counters.add(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Timestamp other) {
		for (int i = 0; i < Math.min(counters.size(), other.counters.size()); i++) {
			if (this.counters.get(i) > other.counters.get(i))
				return 1;
			if (this.counters.get(i) < other.counters.get(i))
				return -1;
		}
		return 0;
	}

	/**
	 * @param timestamp
	 */
	public void copy(Timestamp other) {
		for (int i = 0; i < Math.min(counters.size(), other.counters.size()); i++)
			this.counters.set(i, other.counters.get(i));
	}

	@Override
	public String toString() {
		return "Timestamp: " + this.counters.toString();
	}

}
