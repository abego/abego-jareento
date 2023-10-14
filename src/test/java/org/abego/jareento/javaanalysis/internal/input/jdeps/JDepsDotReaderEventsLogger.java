/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.jareento.javaanalysis.internal.input.jdeps;


class JDepsDotReaderEventsLogger implements JDepsDotReader.EventHandler {
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void onStart(String graphName, int lineNumber) {
        sb.append(lineNumber);
        sb.append('\t');
        sb.append("Start digraph ");
        sb.append(graphName);
        sb.append('\n');
    }

    @Override
    public void onEnd(int lineNumber) {
        sb.append(lineNumber);
        sb.append('\t');
        sb.append("}");
        sb.append('\n');
    }

    @Override
    public void onComment(String commentText, int lineNumber) {
        sb.append(lineNumber);
        sb.append('\t');
        sb.append("// ");
        sb.append(commentText);
        sb.append('\n');
    }

    @Override
    public void onEdge(String from, String to, int lineNumber) {
        sb.append(lineNumber);
        sb.append('\t');
        sb.append(from);
        sb.append(" -> ");
        sb.append(to);
        sb.append('\n');
    }

    String getText() {
        return sb.toString();
    }
}
