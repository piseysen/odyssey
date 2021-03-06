/*
 * Copyright (C) 2016  Hendrik Borghorst & Frederik Luetkes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gateshipone.odyssey.utils;


import android.content.Context;
import android.net.Uri;

import org.gateshipone.odyssey.models.FileModel;
import org.gateshipone.odyssey.models.TrackModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PLSParser extends PlaylistParser {
    private static final String TAG = PLSParser.class.getSimpleName();

    private final FileModel mFile;

    public PLSParser(FileModel file) {
        mFile = file;
    }

    @Override
    public ArrayList<TrackModel> parseList(Context context) {
        Uri uri = FormatHelper.encodeURI(mFile.getPath());
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (null == inputStream) {
            return new ArrayList<>();
        }

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));

        // Try to check if file paths in playlist are relativ or absolute
        String line = "";
        try {
            line = bufReader.readLine();
            while (!line.startsWith("File")) {
                line = bufReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String tmpPath = line.substring(line.indexOf('=') + 1);

        String pathPrefix = "";

        File tmpFile = new File(tmpPath);
        if (!tmpFile.exists()) {
            String plPath = uri.getPath();
            plPath = plPath.substring(0, plPath.lastIndexOf('/'));
            while (!plPath.isEmpty()) {
                tmpFile = new File(plPath + '/' + tmpPath);
                if (!tmpFile.exists() && plPath.contains("/")) {
                    plPath = plPath.substring(0, plPath.lastIndexOf('/'));
                } else {
                    pathPrefix = plPath;
                    break;
                }
            }

        }

        ArrayList<String> urls = new ArrayList<>();
        while (line != null) {
            if (!line.startsWith("File")) {
                try {
                    line = bufReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            tmpPath = line.substring(line.indexOf('=') + 1);
            String tmpUrl;
            if (!pathPrefix.isEmpty()) {
                tmpUrl = pathPrefix + '/' + tmpPath;
            } else {
                tmpUrl = tmpPath;
            }
            if (new File(tmpUrl).exists()) {
                urls.add(tmpUrl);
            }
            try {
                line = bufReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        

        return createTrackModels(context, urls);
    }
}
